package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class UserTile extends Rectangle{

	public UserTile(int x, int y) {
		
		setWidth(Main.TILE_SIZE);
		setHeight(Main.TILE_SIZE);
		relocate(x * Main.TILE_SIZE,y * Main.TILE_SIZE);
		
		setFill(Color.valueOf("#3678F5"));
	}
}
