/*
 *   This file is part of Paintroid, a software part of the Catroid project.
 *   Copyright (C) 2010  Catroid development team
 *   <http://code.google.com/p/catroid/wiki/Credits>
 *
 *   Paintroid is free software: you can redistribute it and/or modify it
 *   under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.tugraz.ist.paintroid.ui.implementation;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import at.tugraz.ist.paintroid.ui.Perspective;

/**
 * The purpose of this class is to provide an independent interface to manipulate the scale and translation of the
 * DrawingSurface. The direct manipulation of the Canvas is synchronized on the SurfaceHolder on which the
 * DrawingSurface must also synchronize its own drawing.
 */
public class DrawingSurfacePerspective implements Perspective {
	public static final float MIN_SCALE = 0.5f;
	public static final float MAX_SCALE = 15f;
	public static final float SCROLL_BORDER = 10f;

	private PointF surfaceCenter;
	private PointF surfaceTranslation;
	private Rect surfaceFrame;
	private float surfaceScale;

	public DrawingSurfacePerspective(SurfaceHolder holder) {
		reset(holder);
	}

	@Override
	public void reset(SurfaceHolder holder) {
		surfaceFrame = holder.getSurfaceFrame();
		surfaceCenter = new PointF(surfaceFrame.exactCenterX(), surfaceFrame.exactCenterY());
		surfaceTranslation = new PointF(0f, 0f);
		surfaceScale = 1f;
	}

	@Override
	public void setScale(float scale) {
		if (scale >= MIN_SCALE) {
			surfaceScale = scale;
		} else {
			surfaceScale = MIN_SCALE;
		}
	}

	@Override
	public void multiplyScale(float factor) {
		surfaceScale *= factor;
		if (surfaceScale < MIN_SCALE) {
			surfaceScale = MIN_SCALE;
		} else if (surfaceScale > MAX_SCALE) {
			surfaceScale = MAX_SCALE;
		}
	}

	@Override
	public void translate(float dx, float dy) {
		surfaceTranslation.offset(Math.round(dx / surfaceScale), Math.round(dy / surfaceScale));

		float xmax = (surfaceFrame.right - surfaceCenter.x - SCROLL_BORDER) / surfaceScale + surfaceCenter.x;
		if (surfaceTranslation.x > xmax) {
			surfaceTranslation.x = xmax;
		} else if (surfaceTranslation.x < -xmax) {
			surfaceTranslation.x = -xmax;
		}

		float ymax = (surfaceFrame.bottom - surfaceCenter.y - SCROLL_BORDER) / surfaceScale + surfaceCenter.y;
		if (surfaceTranslation.y > ymax) {
			surfaceTranslation.y = ymax;
		} else if (surfaceTranslation.y < -ymax) {
			surfaceTranslation.y = -ymax;
		}
	}

	@Override
	public void convertFromScreenToCanvas(Point p) {
		p.x = (int) ((p.x - surfaceCenter.x) / surfaceScale + surfaceCenter.x - surfaceTranslation.x);
		p.y = (int) ((p.y - surfaceCenter.y) / surfaceScale + surfaceCenter.y - surfaceTranslation.y);
	}

	@Override
	public void convertFromScreenToCanvas(PointF p) {
		p.x = (p.x - surfaceCenter.x) / surfaceScale + surfaceCenter.x - surfaceTranslation.x;
		p.y = (p.y - surfaceCenter.y) / surfaceScale + surfaceCenter.y - surfaceTranslation.y;
	}

	@Override
	public void applyToCanvas(Canvas canvas) {
		canvas.scale(surfaceScale, surfaceScale, surfaceCenter.x, surfaceCenter.y);
		canvas.translate(surfaceTranslation.x, surfaceTranslation.y);
	}
}