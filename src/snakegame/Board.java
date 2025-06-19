package snakegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Board extends JPanel implements ActionListener {
    private Image apple, dot, head;
    private final int ALL_DOTS = 900;
    private final int DOT_SIZE = 10;
    private final int RANDOM_POSITION = 29;
    private int apple_x, apple_y;
    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];
    private int dots;
    private boolean leftDirection = false, rightDirection = true, upDirection = false, downDirection = false;
    private Timer timer;
    private int difficulty = 1; // 1 = Easy, 2 = Medium, 3 = Hard
    private final int BASE_DELAY = 150;
    private List<Point> obstacles = new ArrayList<>();

    Board() {
        setBackground(Color.BLACK);
        setFocusable(true);
        loadImages();
        initGame();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                changeDirection(e.getKeyCode());
            }
        });
    }

    public void loadImages() {
        apple = new ImageIcon(ClassLoader.getSystemResource("snakegame/icons/apple.png")).getImage();
        dot = new ImageIcon(ClassLoader.getSystemResource("snakegame/icons/dot.png")).getImage();
        head = new ImageIcon(ClassLoader.getSystemResource("snakegame/icons/head.png")).getImage();
    }

    public void initGame() {
        dots = 3;
        for (int i = 0; i < dots; i++) {
            y[i] = 50;
            x[i] = 50 - i * DOT_SIZE;
        }
        locateApple();
        generateObstacles();
        timer = new Timer(BASE_DELAY, this);
        timer.start();
    }

    public void locateApple() {
        int r = (int) (Math.random() * RANDOM_POSITION);
        apple_x = r * DOT_SIZE;
        r = (int) (Math.random() * RANDOM_POSITION);
        apple_y = r * DOT_SIZE;
    }

    public void generateObstacles() {
        obstacles.clear();
        for (int i = 0; i < 5; i++) { // Add 5 obstacles
            int obsX = (int) (Math.random() * getWidth() / DOT_SIZE) * DOT_SIZE;
            int obsY = (int) (Math.random() * getHeight() / DOT_SIZE) * DOT_SIZE;
            obstacles.add(new Point(obsX, obsY));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(apple, apple_x, apple_y, this);
        
        for (Point obs : obstacles) {
            g.setColor(Color.RED);
            g.fillRect(obs.x, obs.y, DOT_SIZE, DOT_SIZE);
        }
        
        for (int i = 0; i < dots; i++) {
            if (i == 0) {
                g.drawImage(head, x[i], y[i], this);
            } else {
                g.drawImage(dot, x[i], y[i], this);
            }
        }
        
        Toolkit.getDefaultToolkit().sync();
    }

    public void move() {
        for (int i = dots; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        if (leftDirection) x[0] -= DOT_SIZE;
        else if (rightDirection) x[0] += DOT_SIZE;
        else if (upDirection) y[0] -= DOT_SIZE;
        else if (downDirection) y[0] += DOT_SIZE;

        checkCollision();
        repaint();
    }

    public void checkCollision() {
        if (x[0] >= getWidth() || x[0] < 0 || y[0] >= getHeight() || y[0] < 0) {
            gameOver();
        }

        for (int i = dots; i > 0; i--) {
            if ((i > 3) && (x[0] == x[i]) && (y[0] == y[i])) {
                gameOver();
            }
        }

        for (Point obs : obstacles) {
            if (x[0] == obs.x && y[0] == obs.y) {
                gameOver();
            }
        }

        if (x[0] == apple_x && y[0] == apple_y) {
            dots++;
            locateApple();
            playSound("sounds/eat.wav");
            if (dots % 5 == 0) timer.setDelay(Math.max(BASE_DELAY - dots * 2, 50)); // Speed boost every 5 apples
        }
    }

    public void changeDirection(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT && !rightDirection) {
            leftDirection = true;
            upDirection = downDirection = false;
        } else if (keyCode == KeyEvent.VK_RIGHT && !leftDirection) {
            rightDirection = true;
            upDirection = downDirection = false;
        } else if (keyCode == KeyEvent.VK_UP && !downDirection) {
            upDirection = true;
            leftDirection = rightDirection = false;
        } else if (keyCode == KeyEvent.VK_DOWN && !upDirection) {
            downDirection = true;
            leftDirection = rightDirection = false;
        }
    }

    public void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void gameOver() {
        timer.stop();
        playSound("sounds/gameover.wav");
        int choice = JOptionPane.showConfirmDialog(this, "Game Over! Restart?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        }
    }

    public void restartGame() {
        dots = 3;
        for (int i = 0; i < dots; i++) {
            y[i] = 50;
            x[i] = 50 - i * DOT_SIZE;
        }
        leftDirection = false;
        rightDirection = true;
        upDirection = false;
        downDirection = false;
        
        locateApple();
        generateObstacles();
        timer.start();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
    }
}