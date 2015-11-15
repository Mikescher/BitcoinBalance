package samdev.de.bitcoinbalance.views;

public class QRCodelet {
    private final static float SHRINK_SPEED = 1f  /  150;
    private final static float GROW_SPEED   = 1f  /  450;
    private final static float MOVE_SPEED   = 34f / 2000;

    public float posX;
    public float posY;

    public int targetX;
    public int targetY;

    public float transformation = 1f; //1f == rectangle

    public float speedMultiplier = 1f;
    public float movementMultiplier = 1f;
    public boolean deleteAfterMovement = false;

    public QRCodelet(QRCodelet other) {
        posX = other.posX;
        posY = other.posY;

        targetX = other.targetX;
        targetY = other.targetY;

        transformation = other.transformation;
    }

    public QRCodelet(float x, float y, int tx, int ty, boolean transformed) {
        posX = x;
        posY = y;

        targetX = tx;
        targetY = ty;

        transformation = transformed ? 0f : 1f;
    }

    public boolean update(long delta) {
        if (posX == targetX && posY == targetY) {

            if (transformation == 1f) {
                // ON TARGET

                return true;
            } else {
                // GROW

                transformation = Math.min(1, transformation + speedMultiplier * delta * GROW_SPEED);

                return false;
            }

        } else {
            if (transformation == 0) {
                // MOVE

                // Yeah I know were working with Manhatten distances - deal with it

                float cx = movementMultiplier * speedMultiplier * Math.abs(targetX - posX) / (float)Math.sqrt(Math.pow((targetX - posX), 2) + Math.pow((targetY - posY), 2));
                float cy = movementMultiplier * speedMultiplier * Math.abs(targetY - posY) / (float)Math.sqrt(Math.pow((targetX - posX), 2) + Math.pow((targetY - posY), 2));

                if (Math.abs(targetX - posX) <= cx * delta * MOVE_SPEED) {
                    posX = targetX;
                } else {
                    posX += Math.signum(targetX - posX) * (cx * delta * MOVE_SPEED);
                }

                if (Math.abs(targetY - posY) <= cy * delta * MOVE_SPEED) {
                    posY = targetY;
                } else {
                    posY += Math.signum(targetY - posY) * (cy * delta * MOVE_SPEED);
                }

                return false;
            } else {
                // SHRINK

                transformation = Math.max(0, transformation - speedMultiplier * delta * SHRINK_SPEED);

                return false;
            }
        }
    }
}
