// Original Source:
// https://github.com/Mechanical-Advantage/AdvantageKit/tree/main/example_projects/advanced_swerve_drive/src/main, Copyright 2021-2024 FRC 6328
// Modified by 5516 Iron Maple https://github.com/Shenzhen-Robotics-Alliance/

package frc.robot.subsystems.drive;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import frc.robot.Constants;
import org.littletonrobotics.junction.Logger;

import java.util.Queue;

/**
 * Module IO implementation for Talon FX drive motor controller, Talon FX turn motor controller, and
 * CANcoder
 *
 * <p>NOTE: This implementation should be used as a starting point and adapted to different hardware
 * configurations (e.g. If using an analog encoder, copy from "ModuleIOSparkMax")
 *
 * <p>To calibrate the absolute encoder offsets, point the modules straight (such that forward
 * motion on the drive motor will propel the robot forward) and copy the reported values from the
 * absolute encoders using AdvantageScope. These values are logged under
 * "/Drive/ModuleX/TurnAbsolutePositionRad"
 */
public class ModuleIOTalonFX implements ModuleIO {
    private final int index;
    private final TalonFX driveTalon;
    private final TalonFX steerTalon;
    private final CANcoder cancoder;

    private final Queue<Double> driveEncoderUngearedRevolutions;
    private final StatusSignal<Double> driveEncoderUngearedRevolutionsPerSecond;
    private final StatusSignal<Double> driveMotorAppliedVoltage;
    private final StatusSignal<Double> driveMotorCurrent;

    private final Queue<Double> steerEncoderAbsolutePositionRevolutions;
    private final StatusSignal<Double> steerEncoderVelocityRevolutionsPerSecond;
    private final StatusSignal<Double> steerMotorAppliedVolts;
    private final StatusSignal<Double> steerMotorCurrent;

    // Gear ratios for SDS MK4i L2, adjust as necessary
    private final double DRIVE_GEAR_RATIO = (50.0 / 14.0) * (17.0 / 27.0) * (45.0 / 15.0);

    private final boolean isTurnMotorInverted = true;
    private final Rotation2d absoluteEncoderOffset;

    public ModuleIOTalonFX(int index) {
        this.index = index;
        switch (index) {
            case 0 -> {
                driveTalon = new TalonFX(3, Constants.ChassisConfigs.CHASSIS_CANBUS);
                steerTalon = new TalonFX(4, Constants.ChassisConfigs.CHASSIS_CANBUS);
                cancoder = new CANcoder(10, Constants.ChassisConfigs.CHASSIS_CANBUS);
                absoluteEncoderOffset = new Rotation2d(3.3195344249845276); // MUST BE CALIBRATED
            }
            case 1 -> {
                driveTalon = new TalonFX(6, Constants.ChassisConfigs.CHASSIS_CANBUS);
                steerTalon = new TalonFX(5, Constants.ChassisConfigs.CHASSIS_CANBUS);
                cancoder = new CANcoder(11, Constants.ChassisConfigs.CHASSIS_CANBUS);
                absoluteEncoderOffset = new Rotation2d(1.7564080021290591); // MUST BE CALIBRATED
            }
            case 2 -> {
                driveTalon = new TalonFX(1, Constants.ChassisConfigs.CHASSIS_CANBUS);
                steerTalon = new TalonFX(2, Constants.ChassisConfigs.CHASSIS_CANBUS);
                cancoder = new CANcoder(9, Constants.ChassisConfigs.CHASSIS_CANBUS);
                absoluteEncoderOffset = new Rotation2d(0.34974761963792617); // MUST BE CALIBRATED
            }
            case 3 -> {
                driveTalon = new TalonFX(8, Constants.ChassisConfigs.CHASSIS_CANBUS);
                steerTalon = new TalonFX(7, Constants.ChassisConfigs.CHASSIS_CANBUS);
                cancoder = new CANcoder(12, Constants.ChassisConfigs.CHASSIS_CANBUS);
                absoluteEncoderOffset = new Rotation2d(0.10737865515199488); // MUST BE CALIBRATED
            }
            default -> throw new RuntimeException("Invalid module index");
        }

        var driveConfig = new TalonFXConfiguration();
        driveConfig.CurrentLimits.SupplyCurrentLimit = 40.0;
        driveConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
        driveTalon.getConfigurator().apply(driveConfig);
        setDriveBrakeMode(true);

        var turnConfig = new TalonFXConfiguration();
        turnConfig.CurrentLimits.SupplyCurrentLimit = 30.0;
        turnConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
        steerTalon.getConfigurator().apply(turnConfig);
        setTurnBrakeMode(true);

        driveEncoderUngearedRevolutions = OdometryThread.registerSignalInput(driveTalon.getPosition());
        driveEncoderUngearedRevolutionsPerSecond = driveTalon.getVelocity();
        driveMotorAppliedVoltage = driveTalon.getMotorVoltage();
        driveMotorCurrent = driveTalon.getSupplyCurrent();

        steerEncoderAbsolutePositionRevolutions = OdometryThread.registerSignalInput(cancoder.getAbsolutePosition());
        steerEncoderVelocityRevolutionsPerSecond = cancoder.getVelocity();
        steerMotorAppliedVolts = steerTalon.getMotorVoltage();
        steerMotorCurrent = steerTalon.getSupplyCurrent();

        BaseStatusSignal.setUpdateFrequencyForAll(
                50.0,
                driveEncoderUngearedRevolutionsPerSecond,
                driveMotorAppliedVoltage,
                driveMotorCurrent,
                steerEncoderVelocityRevolutionsPerSecond,
                steerMotorAppliedVolts,
                steerMotorCurrent);
        driveTalon.optimizeBusUtilization();
        steerTalon.optimizeBusUtilization();
    }

    @Override
    public void updateInputs(ModuleIOInputs inputs) {
        BaseStatusSignal.refreshAll(
                driveEncoderUngearedRevolutionsPerSecond,
                driveMotorAppliedVoltage,
                driveMotorCurrent,
                steerEncoderVelocityRevolutionsPerSecond,
                steerMotorAppliedVolts,
                steerMotorCurrent);

        long nanos = System.nanoTime();
        inputs.odometryDriveWheelRevolutions = driveEncoderUngearedRevolutions.stream()
                .mapToDouble(value -> value / DRIVE_GEAR_RATIO)
                .toArray();
        driveEncoderUngearedRevolutions.clear();
        inputs.odometrySteerPositions = steerEncoderAbsolutePositionRevolutions.stream()
                .map(this::getSteerFacingFromCANCoderReading)
                .toArray(Rotation2d[]::new);
        steerEncoderAbsolutePositionRevolutions.clear();

        Logger.recordOutput(Constants.LogConfigs.SYSTEM_PERFORMANCE_PATH + "Module" + index + "/Odometry IO Stream CPU TimeMS", (System.nanoTime() - nanos) * 0.000001);

        nanos = System.nanoTime();

        if (inputs.odometryDriveWheelRevolutions.length > 0)
            inputs.driveWheelFinalRevolutions = inputs.odometryDriveWheelRevolutions[inputs.odometryDriveWheelRevolutions.length-1];

        inputs.driveWheelFinalVelocityRevolutionsPerSec = Units.rotationsToRadians(driveEncoderUngearedRevolutionsPerSecond.getValueAsDouble()) / DRIVE_GEAR_RATIO;
        inputs.driveMotorAppliedVolts = driveMotorAppliedVoltage.getValueAsDouble();
        inputs.driveMotorCurrentAmps = driveMotorCurrent.getValueAsDouble();
        Logger.recordOutput(Constants.LogConfigs.SYSTEM_PERFORMANCE_PATH + "Module" + index + "/Drive IO CPU TimeMS", (System.nanoTime() - nanos) * 0.000001);

        if (inputs.odometrySteerPositions.length > 0)
            inputs.steerFacing = inputs.odometrySteerPositions[inputs.odometrySteerPositions.length-1];
        inputs.steerVelocityRadPerSec = Units.rotationsToRadians(steerEncoderVelocityRevolutionsPerSecond.getValueAsDouble());
        inputs.steerMotorAppliedVolts = steerMotorAppliedVolts.getValueAsDouble();
        inputs.steerMotorCurrentAmps = steerMotorCurrent.getValueAsDouble();
    }

    private Rotation2d getSteerFacingFromCANCoderReading(double canCoderReadingRotations) {
        return Rotation2d.fromRotations(canCoderReadingRotations).minus(absoluteEncoderOffset);
    }

    @Override
    public void setDrivePower(double power) {
        driveTalon.set(power);
    }

    @Override
    public void setSteerPower(double power) {
       steerTalon.set(power);
    }

    @Override
    public void setDriveBrakeMode(boolean enable) {
        var config = new MotorOutputConfigs();
        config.Inverted = InvertedValue.CounterClockwise_Positive;
        config.NeutralMode = enable ? NeutralModeValue.Brake : NeutralModeValue.Coast;
        driveTalon.getConfigurator().apply(config);
    }

    @Override
    public void setTurnBrakeMode(boolean enable) {
        var config = new MotorOutputConfigs();
        config.Inverted = isTurnMotorInverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
        config.NeutralMode = enable ? NeutralModeValue.Brake : NeutralModeValue.Coast;
        steerTalon.getConfigurator().apply(config);
    }
}
