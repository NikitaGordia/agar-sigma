package com.sigma.agar.utils.protocol;

import com.sigma.agar.physics.entity.Thing;
import com.sigma.agar.utils.Tuple;
import org.web.httpserver.Session;

import java.util.List;

public interface Protocol {
	void dispatch(Tuple center, float width, float height, List<Thing> entities, Session session);
}
