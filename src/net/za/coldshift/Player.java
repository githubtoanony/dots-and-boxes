package net.za.coldshift;

import java.util.ArrayList;

import org.cocos2d.types.ccColor4B;

public class Player {
	// player name
	public String name = "";
	
	// completed shapes - used to keep score
	public ArrayList<ShapeModel> shapes = new ArrayList<ShapeModel>();
	
	// player color
	public ccColor4B color;
	
	// player sprite image
	public String spriteImage;
	
	public Player(String name, ccColor4B color)
	{
		this.name = name;
		this.color = color;
		
		// get sprite based on color
		this.spriteImage = "player_" + 
						   "r" + color.r +
						   "g" + color.g + 
						   "b" + color.b;
	}
	
	public void addShape(ShapeModel shape)
	{
		this.shapes.add(shape);
		shape.takeOwnership(this);
	}
	
}
