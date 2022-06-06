package application;

import java.util.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.awt.Point;

public class Main extends Application {
	static int rows = 15;
	static int columns = 15;
	static int tilesize = 50;
	static int speed = 8;
	static int direction, color;
	static ArrayList<Point> snake = new ArrayList<Point>();
	static Point apple = new Point();
	static final Color[] colors = { Color.MEDIUMPURPLE, Color.ORANGE, Color.PINK, Color.MAROON, Color.GOLD,
			Color.LIGHTSKYBLUE, Color.STEELBLUE };
	static Timer t;
	static ArrayList<KeyEvent> presses = new ArrayList<KeyEvent>();
	static ArrayList<KeyCode> keys = new ArrayList<KeyCode>();
	static boolean gameOver = false;
	static GraphicsContext gc;

	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, columns * tilesize, (rows + 1) * tilesize);	// rows + 1 to add space for the score banner
			Canvas canvas = new Canvas(columns * tilesize, (rows + 1) * tilesize);
			root.getChildren().add(canvas);
			gc = canvas.getGraphicsContext2D();
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setTextBaseline(VPos.CENTER);
			keys.add(KeyCode.UP);
			keys.add(KeyCode.LEFT);
			keys.add(KeyCode.DOWN);
			keys.add(KeyCode.RIGHT);
			keys.add(KeyCode.W);
			keys.add(KeyCode.A);
			keys.add(KeyCode.S);
			keys.add(KeyCode.D);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
				if (gameOver) {
					if (e.getCode() == KeyCode.R) {
						snake.clear();
						presses.clear();
						setup();
					} else if (e.getCode() == KeyCode.Q)
						System.exit(0);
				} else {
					if (keys.contains(e.getCode()) &&	// Make sure you can't input an invalid direction (e.g. up and then up or down)
							(presses.isEmpty() || (keys.indexOf(presses.get(presses.size() - 1).getCode()) + keys.indexOf(e.getCode())) % 2 != 0))
						presses.add(e);
				}
			});
			setup();
			primaryStage.setTitle("Snake");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setup() {
		gameOver = false;
		direction = color = 0;
		for (int i = 3; i <= 5; i++)	// Initialize snake with length 3
			snake.add(new Point(columns / 2 - i, rows / 2));
		generateApple();
		t = new Timer();
		t.start();
	}

	public static void updateFrame() {
		gc.setFill(Color.GREEN);	// Draw score banner
		gc.fillRect(0, 0, columns * tilesize, tilesize);
		for (int i = 0; i < columns; i++)	// Draw checkered background
			for (int j = 0; j < rows; j++) {
				if ((i + j) % 2 == 0)
					gc.setFill(Color.web("AAD753"));
				else
					gc.setFill(Color.web("A2D14B"));
				gc.fillRect(i * tilesize, (j + 1) * tilesize, tilesize, tilesize);
			}
		for (int i = snake.size() - 1; i >= 1; i--)		// Update snake position
			snake.get(i).setLocation(snake.get(i - 1));
		Point first = new Point(snake.get(0));
		if (direction == 0)
			first.x++;
		else if (direction == 1)
			first.y--;
		else if (direction == 2)
			first.x--;
		else
			first.y++;
		if (first.x < 0 || first.y < 0 || first.x >= columns || first.y >= rows) {		// Collision with wall
			gameOver(false);
			return;
		}
		snake.set(0, first);

		boolean ate = false;
		if (apple.equals(snake.get(0))) {	// Ate an apple
			ate = true;
			Point last = snake.get(snake.size() - 1);
			if (last.x < snake.get(snake.size() - 2).x)
				snake.add(new Point(last.x - 1, last.y));
			else if (last.x > snake.get(snake.size() - 2).x)
				snake.add(new Point(last.x + 1, last.y));
			else if (last.y < snake.get(snake.size() - 2).y)
				snake.add(new Point(last.x, last.y - 1));
			else
				snake.add(new Point(last.x, last.y + 1));
			if(snake.size() < rows * columns)
				generateApple();
		}

		for (int i = 1; i < snake.size(); i++)		// Ran into itself
			if (snake.get(0).x == snake.get(i).x && snake.get(0).y == snake.get(i).y) {
				gameOver(false);
				return;
			}

		gc.setFill(colors[color]);	// Draw apple
		gc.fillOval(apple.x * tilesize, (apple.y + 1) * tilesize, tilesize - 2, tilesize - 2);

		drawSnake(ate);		// Draw snake
		
		updateScore();		
		if (snake.size() == rows * columns)		// Game won
			gameOver(true);
	}

	public static void updateScore() {
		gc.setFill(Color.WHITE);
		gc.setFont(new Font("Monospaced Bold Italic", 30));
		gc.fillText("Score: " + (snake.size() - 3), columns * tilesize / 2, tilesize / 2);
	}

	public static void updateDirection(KeyEvent key) {
		if ((key.getCode() == KeyCode.W || key.getCode() == KeyCode.UP) && direction != 3)
			direction = 1;
		else if ((key.getCode() == KeyCode.A || key.getCode() == KeyCode.LEFT) && direction != 0)
			direction = 2;
		else if ((key.getCode() == KeyCode.S || key.getCode() == KeyCode.DOWN) && direction != 1)
			direction = 3;
		else if ((key.getCode() == KeyCode.D || key.getCode() == KeyCode.RIGHT) && direction != 2)
			direction = 0;
	}
	
	public static void drawSnake(boolean ate) {
		gc.setFill(Color.rgb(0, 160, 255));
		gc.fillOval(snake.get(0).x * tilesize, (snake.get(0).y + 1) * tilesize, tilesize - 2, tilesize - 2);	// Draw head
		int limit = snake.size();
		if(ate)		// If ate an apple, don't draw the end segment that was just added until the next frame
			limit--;
		for (int i = 1; i < limit; i++) {	// Can adjust to i > 32 and i * 5 for a faster color gradient change
			if (i > 80)
				gc.setFill(Color.BLUE);
			else
				gc.setFill(Color.rgb(0, 160 - i * 2, 255)); // Blue gradient
			gc.fillRoundRect(snake.get(i).x * tilesize, (snake.get(i).y + 1) * tilesize, tilesize - 2, tilesize - 2, 30, 30);
		}
	}

	public static void gameOver(boolean won) {
		if(won) {	// Make sure the last segment is in the right place, as when you win there is only one spot it could go
			snake.remove(snake.size() - 1);
			Point s = snake.get(snake.size() - 1);
			if(s.x + 1 >= 0 && s.x + 1 < columns && !snake.contains(new Point(s.x + 1, s.y)))
				snake.add(snake.size() - 1, new Point(s.x + 1, s.y));
			else if(s.x - 1 >= 0 && s.x - 1 < columns && !snake.contains(new Point(s.x - 1, s.y)))
				snake.add(snake.size() - 1, new Point(s.x - 1, s.y));
			else if(s.y + 1 >= 0 && s.y + 1 < rows && !snake.contains(new Point(s.x, s.y + 1)))
				snake.add(snake.size() - 1, new Point(s.x, s.y + 1));
			else
				snake.add(snake.size() - 1, new Point(s.x, s.y - 1));
		}
		drawSnake(false);
		updateScore();
		gc.setFont(new Font("Monospaced Bold Italic", 50));
		if (won) {
			gc.setFill(Color.GREEN);
			gc.fillText("YOU WIN!", columns * tilesize / 2, rows / 2.0 * tilesize);
		} else {
			gc.setFill(Color.RED);
			gc.fillOval(snake.get(0).x * tilesize, (snake.get(0).y + 1) * tilesize, tilesize - 2, tilesize - 2);
			gc.fillText("GAME OVER", columns * tilesize / 2, rows / 2.0 * tilesize);
		}
		gc.setFont(new Font("Monospaced Bold Italic", 30));
		gc.fillText("Press R to restart or Q to quit", columns * tilesize / 2, (rows + 2) / 2.0 * tilesize);
		gameOver = true;
		t.stop();
	}

	public static void generateApple() {
		while (true) {
			boolean inSnake = false;
			apple.setLocation((int) (Math.random() * columns), (int) (Math.random() * rows));
			for (Point c : snake)
				if (c.equals(apple)) {
					inSnake = true;
					break;
				}
			if (!inSnake)
				break;
		}
		int prev = color;
		while (color == prev)
			color = (int) (Math.random() * colors.length);
	}

	public static void main(String[] args) {
		launch(args);
	}

	static class Timer extends AnimationTimer {
		private double prev = 0;

		public void handle(long now) {
			if ((now / 1000000000.0 - prev) * speed > 1 || prev == 0) {
				prev = now / 1000000000.0;
				if (!presses.isEmpty())
					updateDirection(presses.remove(0));		// Grab the next keypress from the queue
				updateFrame();
			}
		}
	}
}