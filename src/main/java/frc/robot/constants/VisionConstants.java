package frc.robot.constants;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.subsystems.vision.apriltags.PhotonCameraProperties;

import java.util.List;

public class VisionConstants {
    public static final List<PhotonCameraProperties> photonVisionCameras = List.of(
            new PhotonCameraProperties(
                    "FrontCam",
                        30, 18, 5,
                       68,
                        0.6, 0.2,
                        1280, 720,
                        new Translation2d(0.2, 0),
                        0.3,
                        Rotation2d.fromDegrees(0),
                        24,
                        180 // upside-down
            ),
            new PhotonCameraProperties(
                    "FrontLeftCam",
                    30, 18, 5,
                    68,
                    0.6, 0.2,
                    1280, 720,
                    new Translation2d(0.2, 0.15),
                    0.3,
                    Rotation2d.fromDegrees(35),
                    30,
                    180 // upside-down
            ),
            new PhotonCameraProperties(
                    "FrontRightCam",
                    30, 18, 5,
                    68,
                    0.6, 0.2,
                    1280, 720,
                    new Translation2d(0.2, -0.15),
                    0.3,
                    Rotation2d.fromDegrees(-35),
                    30,
                    180 // upside-down
            ),
            new PhotonCameraProperties(
                    "BackLeftCam",
                    30, 18, 5,
                    68,
                    0.6, 0.2,
                    1280, 720,
                    new Translation2d(-0.2, 0.15),
                    0.3,
                    Rotation2d.fromDegrees(60),
                    30,
                    180 // upside-down
            ),
            new PhotonCameraProperties(
                    "BackRightCam",
                    30, 18, 5,
                    68,
                    0.6, 0.2,
                    1280, 720,
                    new Translation2d(-0.2, -0.15),
                    0.3,
                    Rotation2d.fromDegrees(-60),
                    30,
                    180 // upside-down
            )
    );
}
