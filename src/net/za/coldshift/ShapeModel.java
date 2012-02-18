package net.za.coldshift;

import java.util.ArrayList;

import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;

import android.util.Log;

public class ShapeModel extends CCNode {
	// list of the dots that make up the shape
	public ArrayList<DotModel> dots = new ArrayList<DotModel>();
	
	// list of lines that have been formed
	public ArrayList<LineModel> lines = new ArrayList<LineModel>();

	// identifier for the shape - useful for debugging
	public String shapeId = "";
	
	// whether the shape is completely active
	public boolean isComplete = false; 
	
	// player who completed the shape
	public Player player;

	public CGPoint areaWH;
	
	private CCSprite sprite;
	
	public ShapeModel()
	{
	}
	
	public ShapeModel(String shapeId)
	{
		this.shapeId = shapeId;
	}

	public void takeOwnership(Player player)
	{
		this.player = player;
		this.isComplete = true;
		
		// update ownership on all shapes which have the same points
		for (DotModel dot : dots) {
			ArrayList<ShapeModel> shapes = dot.neighborhood;
			
			if(shapes.size() > 0)
			{
				for (ShapeModel shape : shapes) {
					boolean same = true;
					
					for (DotModel thisDot : dots) {
						if(!shape.contains(thisDot.dotId))
						{
							same = false;
							break;
						}
					}
					
					if(same)
					{
						shape.player = player;
						shape.isComplete = isComplete;
					}
				}
			}
		}
		
		// find bottom-left point
		
		CGPoint point = CGPoint.make(GameLayer.windowSize.width, GameLayer.windowSize.height);
		
		if(dots.size() > 0)
		{
			for (DotModel dot : dots) {
				if(dot.center.x < point.x && dot.center.y < point.y)
				{
					point = dot.center;
				}
			}
		}
		
		sprite = CCSprite.sprite(player.spriteImage + ".png");
		sprite.setVertexZ(2);
		
		sprite.setPosition(point.x + (areaWH.x / 2),
						   point.y + (areaWH.y / 2)
						  );
		
		int spriteWidth = (int) sprite.getBoundingBox().size.width;
		int spriteHeight = (int) sprite.getBoundingBox().size.height;

		sprite.setScaleX(areaWH.x / spriteWidth);
		sprite.setScaleY(areaWH.y / spriteHeight);
		addChild(sprite);
	}
	
	public boolean contains(String dotId)
	{
		// check if shape is truly complete
		for (DotModel dot : dots) {
			if(dot.dotId.equals(dotId))
			{
				return true;
			}
		}		
		
		return false;
	}
	
	public DotModel getDot(String dotId)
	{
		// check if shape is truly complete
		for (DotModel dot : dots) {
			if(dot.dotId.equals(dotId))
			{
				return dot;
			}
		}		
		
		return null;
	}
	
	/**
	 * check whether shape has been completed
	 * @return
	 */
	public boolean check()
	{
		if(isComplete)
		{
			return true;
		}
		
		// assume that the shape is complete
		boolean complete = true;
		
		// check if shape is truly complete
		for (DotModel dot : dots) {
			if(!dot.isActivated)
			{
				// it's not :(
				complete = false;
				break;
			}
			else
			{
				// possibly complete
				int found = 0;
				
				// each shape must have a minimum of two lines, so check if both lines are set
				for(LineModel line : lines)
				{
					if(line.dot.dotId.equals(dot.dotId) || 
					   line.lastDot.dotId.equals(dot.dotId))
					{
						found++;
					}
				}
				
				if(found < 2)
				{
					complete = false;
					break;
				}
			}
		}

		isComplete = complete;
		
		return isComplete;
	}
	
	public boolean lineExists(DotModel dot, DotModel lastDot)
	{
		// check if linemodel doesn't already exist
		if(lines.size() > 0)
		{
			for (LineModel line : lines) {
				if((line.dot.dotId.equals(dot.dotId) &&
					line.lastDot.dotId.equals(lastDot.dotId)) ||
				   (line.dot.dotId.equals(lastDot.dotId) &&
					line.lastDot.dotId.equals(dot.dotId))
				   )
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void commitLineSegment(LineModel lineModel)
	{
		if(lineExists(lineModel.dot, lineModel.lastDot))
		{
			return;
		}
		
		lines.add(lineModel);
		
		if(dots.size() > 0)
		{
			for (DotModel dot : dots) {
				dot.commit(lineModel);
			}
		}
	}
	
	@Override
	public String toString()
	{
		String response = shapeId + " dots: " ;
		
		for (DotModel dot : dots) {
			response += ", " + dot.dotId + ": " + dot.isActivated;
		}
		
		response += ", lines: ";
		
		for (LineModel line : lines) {
			response += ", " + line.dot.dotId + " - " + line.lastDot.dotId;
		}
		
		return response;
	}
}
