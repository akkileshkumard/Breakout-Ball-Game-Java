package breakoutBall;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Gameplay extends JPanel implements ActionListener, KeyListener {

    private boolean play = false;
    private int score = 0;
    private int totalbricks = 36;

    // Timer
    private Timer timer;
    private int delay = 8;

    // Paddle
    private int playerX = 310;

    // Ball
    private int ballposX = 120;
    private int ballposY = 350;
    private int ballXdir = -1;
    private int ballYdir = -2;

    private MapGenerator map;
    private boolean ballFalling = false;
    private int lives = 5; // Initial number of lives

    public Gameplay() {
        map = new MapGenerator(4, 9);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    public void paint(Graphics g) {
        // Background
        g.setColor(Color.black);
        g.fillRect(1, 1, 692, 592);

        // Drawing map
        map.draw((Graphics2D) g);

        // Borders
        g.setColor(Color.yellow);
        g.fillRect(0, 0, 3, 592);
        g.fillRect(0, 0, 692, 3);
        g.fillRect(681, 0, 3, 592);

        // Scores
        g.setColor(Color.white);
        g.setFont(new Font("serif", Font.BOLD, 25));
        g.drawString("" + score, 590, 30);

        // Paddle
        g.setColor(Color.green);
        g.fillRect(playerX, 550, 100, 8);

        // Ball
        g.setColor(Color.yellow);
        g.fillOval(ballposX, ballposY, 20, 20);

        // Lives
        g.setColor(Color.white);
        g.setFont(new Font("serif", Font.BOLD, 25));
        g.drawString("Lives: " + lives, 10, 30);

        // Game over
        if (totalbricks <= 0) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;
            g.setColor(Color.RED);
            g.setFont(new Font("serif", Font.BOLD, 30));
            g.drawString("You Won", 260, 300);

            g.setColor(Color.RED);
            g.setFont(new Font("serif", Font.BOLD, 20));
            g.drawString("Press (Enter) to Restart", 230, 350);
        }

        // Game over when lives are exhausted
        if (lives <= 0) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;
            g.setColor(Color.RED);
            g.setFont(new Font("serif", Font.BOLD, 30));
            g.drawString("Game Over, Scores: " + score, 190, 300);

            g.setColor(Color.RED);
            g.setFont(new Font("serif", Font.BOLD, 20));
            g.drawString("Press (Enter) to Restart", 230, 350);
        }

        g.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (playerX >= 600) {
                playerX = 600;
            } else {
                moveRight();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (playerX < 10) {
                playerX = 10;
            } else {
                moveLeft();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!play) {
                play = true;
                ballposX = 120;
                ballposY = 350;
                ballXdir = -1;
                ballYdir = -2;
                playerX = 310;
                score = 0;
                totalbricks = 36;
                lives = 5; // Reset lives
                map = new MapGenerator(4, 9);

                repaint();
            }
        }
    }

    public void moveLeft() {
        play = true;
        playerX -= 15;
    }

    public void moveRight() {
        play = true;
        playerX += 15;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        timer.start();
        if (play) {
            // Ball-paddle collision
            if (new Rectangle(ballposX, ballposY, 20, 20).intersects(new Rectangle(playerX, 550, 100, 8))) {
                ballYdir = -ballYdir;
                playSound("paddle.wav"); // Play sound when ball hits paddle
            } else if (new Rectangle(ballposX, ballposY, 20, 20)
                    .intersects(new Rectangle(playerX + 70, 550, 100, 8))) {
                ballYdir = -ballYdir;
                ballXdir = ballXdir + 1;
                playSound("paddle.wav"); // Play sound when ball hits paddle
            } else if (new Rectangle(ballposX, ballposY, 20, 20)
                    .intersects(new Rectangle(playerX + 30, 550, 110, 8))) {
                ballYdir = -ballYdir;
                playSound("paddle.wav"); // Play sound when ball hits paddle
            }

            // Check collision with map bricks
            A: for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle rect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        Rectangle ballRect = new Rectangle(ballposX, ballposY, 20, 20);
                        Rectangle brickRect = rect;

                        if (ballRect.intersects(brickRect)) {
                            map.setBrickValue(0, i, j);
                            totalbricks--;
                            score += 5;

                            // Play sound when ball hits brick
                            playSound("brick.wav");

                            // Add bonus bricks that give extra score
                            if (map.map[i][j] == 2) {
                                score += 10;
                                // Display popup message or animation for bonus brick hit
                                // You can implement this with JOptionPane or custom animation
                            }

                            // Bounce mechanics - increase speed with time and randomize direction
                            if (Math.abs(ballXdir) < 5) {
                                ballXdir += Math.random() > 0.5 ? 1 : -1;
                            }
                            ballYdir = -ballYdir;

                            break A;
                        }
                    }
                }
            }

            ballposX += ballXdir;
            ballposY += ballYdir;

            // Ball movement boundaries
            if (ballposX < 0) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }
            if (ballposX > 670) {
                ballXdir = -ballXdir;
            }

            // Ball falls below paddle - lose life and reset position
            if (ballposY > 570) {
                lives--;
                playSound("lose_life.wav"); // Play sound when losing life
                ballposX = 120;
                ballposY = 350;
                ballXdir = -1;
                ballYdir = -2;
                playerX = 310;
            }

            repaint();
        }

    }

    // Method to play sound from file
    private void playSound(String soundFile) {
        try {
            AudioInputStream audioInputStream = AudioSystem
                    .getAudioInputStream(new File("src/breakoutBall/sounds/" + soundFile).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error playing sound: " + ex.getMessage());
        }
    }

}
