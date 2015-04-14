package org.codemucker.jmutate.generate.model;

import java.lang.reflect.Field;

import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.util.NameUtil;


public class FieldModel extends ModelObject {

	private final String name;
	private final String fullType;
	private final ModifierModel modifiers;

	private TypeModel type;

	public FieldModel(Field f){
		name = f.getName();
		fullType = NameUtil.compiledNameToSourceName(f.getType().getName());
		modifiers = new ModifierModel(f.getModifiers());
	}

	public FieldModel(JField f){
		name = f.getName();
		fullType = f.getFullTypeName();
		modifiers = new ModifierModel(f.getJModifiers());
	}

	public String getName() {
		return name;
	}

	public TypeModel getType() {
		if (type == null) {
			type = new TypeModel(fullType, null);
		}
		return type;
	}

	public ModifierModel getModifiers() {
		return modifiers;
	}

}
