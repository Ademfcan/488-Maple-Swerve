package frc.robot.utils.CompetitionFieldUtils.Simulation;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.Robot;
import frc.robot.utils.CompetitionFieldUtils.FieldObjects.RobotOnField;
import frc.robot.utils.CompetitionFieldUtils.MapleCompetitionField;
import frc.robot.utils.MapleMaths.GeometryConvertor;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * the class that simulates the physical behavior of all the objects on field
 * should only be created during a robot simulation (not in real or replay mode)
 * */
public abstract class CompetitionFieldSimulation {
    private final World<Body> physicsWorld;
    private final MapleCompetitionField competitionField;

    public CompetitionFieldSimulation(RobotOnField mainRobot, FieldObstaclesMap obstaclesMap) {
        this.competitionField = new MapleCompetitionField(mainRobot);

        this.physicsWorld = new World<>();
        this.physicsWorld.setGravity(PhysicsWorld.ZERO_GRAVITY);
        for (Body obstacle: obstaclesMap.obstacles)
            this.physicsWorld.addBody(obstacle);
    }

    public void updateSimulationWorld() {
        this.physicsWorld.step(1, Robot.defaultPeriodSecs);

        competitionField.updateObjectsToDashboardAndTelemetry();
    }

    public void addRobot(HolonomicChassisSimulation chassisSimulation) {
        this.physicsWorld.addBody(chassisSimulation);
        this.competitionField.addObject(chassisSimulation);
    }

    public MapleCompetitionField getCompetitionField() {
        return competitionField;
    }

    /**
     * stores the obstacles on a competition field, which includes the border and the game pieces
     * */
    public static abstract class FieldObstaclesMap {
        private final List<Body> obstacles = new ArrayList<>();

        protected void addBorderLine(Translation2d startingPoint, Translation2d endingPoint) {
            final Body obstacle = getObstacle(Geometry.createSegment(
                    GeometryConvertor.toDyn4jVector2(startingPoint),
                    GeometryConvertor.toDyn4jVector2(endingPoint)
            ));
            obstacles.add(obstacle);
        }

        protected void addRectangularObstacle(double width, double height, Pose2d pose) {
            final Body obstacle = getObstacle(Geometry.createRectangle(
                    width, height
            ));

            obstacle.getTransform().set(GeometryConvertor.toDyn4jTransform(pose));
            obstacles.add(obstacle);
        }

        private static Body getObstacle(Convex shape) {
            final Body obstacle = new Body();
            obstacle.setMass(MassType.INFINITE);
            final BodyFixture fixture = obstacle.addFixture(shape);
            fixture.setFriction(0.8);
            fixture.setRestitution(0.6);
            return obstacle;
        }
    }
}
