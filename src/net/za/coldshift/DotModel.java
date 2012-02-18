package net.za.coldshift;

import java.util.ArrayList;

import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;

import android.util.Log;


public class DotModel extends BaseDotModel {
	private CCSprite sprite;
	private CGRect boundingBox;
	
	public CGPoint center;
	public boolean isActivated = false;
	public String dotId = "";
	
	public Player selectedBy;
	public int move = 0;
	
	public boolean isCommited = false;
	
	// list of neighboring dots stored as a shape 
	public ArrayList<ShapeModel> neighborhood = new ArrayList<ShapeModel>();
	
	/**
	 * Create a dot
	 * @param dotId
	 * @param x 
	 * @param y
	 * @param width
	 * @param height
	 */
	public DotModel(String dotId, int x, int y, int width, int height)
	{
		this.dotId = dotId;
		
		sprite = CCSprite.sprite("dot.png");
		
		// calculate size of sprite
		int spriteWidth = (int) sprite.getBoundingBox().size.width;
		int spriteHeight = (int) sprite.getBoundingBox().size.height;
		
		boundingBox = CGRect.make((float)x, (float)y, (float)width, (float)height);
		
		// center align
		center = CGPoint.make(x + ((width / 2) - (spriteWidth / 2)), y + ((height / 2) - (spriteHeight / 2)));

		showSprite("dot.png");
	}
	
	public boolean nextTo(String dotId)
	{
		if(neighborhood.size() > 0)
		{
			for (ShapeModel shape : neighborhood) {
				return shape.contains(dotId);
			}
		}
		
		return false;
	}

	public DotModel getNeighbor(String dotId)
	{
		if(neighborhood.size() > 0)
		{
			for (ShapeModel shape : neighborhood) {
				DotModel neighbor = shape.getDot(dotId);
				
				if(neighbor != null)
				{
					if(!GameLayer.allowDiagonal)
					{
						// ensure that neighbor isn't diagonal
						if(neighbor.center.x == this.center.x || 
						   neighbor.center.y == this.center.y
						  )
						{
							return neighbor;
						}
						else
						{
							return null;
						}
					}
					
					return neighbor;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * check for completed shapes
	 * @return
	 */
	public ArrayList<ShapeModel> check()
	{
		// check if the entire shape has been completed
		if(neighborhood.size() > 0)
		{
			ArrayList<ShapeModel> completedShapes = new ArrayList<ShapeModel>();
			
			for (ShapeModel shape : neighborhood) {
				if(!shape.isComplete)
				{
					boolean complete = shape.check();
					
					if(complete && shape.player == null)
					{
						completedShapes.add(shape);
					}
				}
			}
			
			if(completedShapes.size() > 0)
			{
				return completedShapes;
			}
		}
			
		return null;
	}
	
	public boolean lineExists(DotModel lastDot)
	{
		// add line model to shape
		if(neighborhood.size() > 0)
		{
			for (ShapeModel shape : neighborhood) {
				boolean exists = shape.lineExists(this, lastDot);
				
				if(exists)
				{
					return true;
				}
			}		
		}
		
		return false;
	}
	
	public boolean hasOpenNeigbors()
	{
		ArrayList<ShapeModel> completed = check();
		
		if(completed == null)
		{
			return true;
		}
		
		return false;
	}
	
	public void activate(Player player, int move)
	{
		this.selectedBy = player;
		this.move = move;
		
		isActivated = true;
		
		// show selected image
		removeChild(sprite, true);
		
		showSprite("dot_highlighted.png");
	}
	
	public void commit(LineModel lineModel)
	{
		// add line model to shape
		for (ShapeModel shape : neighborhood) {
			
			// check if shape contains dot and lastdot
			if(shape.contains(lineModel.dot.dotId) && 
			   shape.contains(lineModel.lastDot.dotId))
			{
				shape.commitLineSegment(lineModel);
			}
		}		
	}

	public void commitAndActivate(LineModel lineModel)
	{
		commit(lineModel);
		isCommited = true;
		
		// show selected image
		removeChild(sprite, true);
		
		showSprite("dot_selected.png");
	}
	
	public void deactivate()
	{

		if(isCommited)
		{
			// show selected image
			removeChild(sprite, true);
			
			showSprite("dot_selected.png");
			return;
		}
	
		this.selectedBy = null;
		this.move = -1;
		
		isActivated = false;
		
		// show selected image
		removeChild(sprite, true);
		
		showSprite("dot.png");
	}
	
	private void showSprite(String spriteImage)
	{
		sprite = CCSprite.sprite(spriteImage);
		sprite.setVertexZ(3);
		
		sprite.setPosition(center);
		addChild(sprite);
	}
	
	@Override
	public CGRect getBoundingBox()
	{
		return boundingBox;
	}
	
	@Override
	public String toString()
	{	
		return dotId;
	}
}
