package org.xwiki.contrib.glossary;

import java.util.Map;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.transformation.TransformationContext;

public interface GlossaryTransformation {

	public Map<String, DocumentReference> getGlossaryEntries();
	
	public void transform(Block block, TransformationContext context);
}
