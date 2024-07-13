package frc.robot.utils.CompetitionFieldUtils.Simulation;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.Constants;
import frc.robot.subsystems.drive.HolonomicDrive;
import frc.robot.utils.MapleJoystickDriveInput;
import org.ejml.simple.UnsupportedOperation;

public class OpponentRobotSimulation extends HolonomicChassisSimulation implements HolonomicDrive {
    private static final HolonomicChassisSimulation.RobotProfile opponentRobotProfile = new RobotProfile(
            4,
            10,
            Math.toRadians(360),
            Constants.RobotPhysicsSimulationConfigs.DEFAULT_ROBOT_MASS,
            Constants.RobotPhysicsSimulationConfigs.DEFAULT_BUMPER_WIDTH_METERS,
            Constants.RobotPhysicsSimulationConfigs.DEFAULT_BUMPER_LENGTH_METERS
    );
    public OpponentRobotSimulation() {
        super(opponentRobotProfile);
    }

    @Override
    public void setPose(Pose2d currentPose) {
        super.setSimulationWorldPose(currentPose);
    }

    @Override public void addVisionMeasurement(Pose2d visionPose, double timestamp) {throw new UnsupportedOperation("an opponent robot does not support vision measurement"); }

    public enum Behavior {
        JOYSTICK_CONTROL,
        AUTO_CYCLE,
        QUEEN
    }

    private static final Pose2d[] redRobotsStartingPositions = new Pose2d[] {
            new Pose2d(15.2, 7, Rotation2d.fromRotations(0.5)),
            new Pose2d(15.2, 4, Rotation2d.fromRotations(0.5)),
            new Pose2d(15.2, 2.5, Rotation2d.fromRotations(0.5))
    };
    /* if an opponent robot is not requested to be on field, it queens outside the field for performance */
    private static final Pose2d robotQueeningPosition = new Pose2d(-5, 5, new Rotation2d());

    // TODO using holonomic drive commands, make the opponent robots drive in the intended modes

    /**
     * a method to test the driving physics
     * just for testing
     * in the formal code, we should be using holonomic drive commands
     * */
    @Deprecated
    public void testDrivingPhysicsWithJoystick(XboxController xboxController) {
        final MapleJoystickDriveInput mapleJoystickDriveInput = MapleJoystickDriveInput.leftHandedJoystick(xboxController);
        final ChassisSpeeds gamePadSpeeds = mapleJoystickDriveInput.getJoystickChassisSpeeds(5, 10);
        HolonomicDrive.super.runDriverStationCentricChassisSpeeds(gamePadSpeeds);
    }
}
