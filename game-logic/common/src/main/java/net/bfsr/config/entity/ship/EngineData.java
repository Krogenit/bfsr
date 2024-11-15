package net.bfsr.config.entity.ship;

import org.jbox2d.collision.shapes.Polygon;
import org.joml.Vector2f;

import java.util.List;

public record EngineData(List<Polygon> polygons, Vector2f effectPosition) {}