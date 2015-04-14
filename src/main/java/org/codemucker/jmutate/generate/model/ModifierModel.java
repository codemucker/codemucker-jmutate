package org.codemucker.jmutate.generate.model;

import java.lang.reflect.Modifier;

import org.codemucker.jmutate.ast.JModifier;

public class ModifierModel {
	private int mods;

	public ModifierModel(int mods) {
		super();
		this.mods = mods;
	}

	public ModifierModel(JModifier m) {
		super();
		this.mods = 0;
		setAbstract(m.isAbstract());
		setFinal(m.isFinal());
		setStatic(m.isStatic());
		setNative(m.isNative());
		setSynchronized(m.isSynchronized());
		setTransient(m.isTransient());
		setVolatile(m.isVolatile());
		setStrict(m.isStrictFp());

		if (m.isPublic()) {
			setPublic();
		} else if (m.isPrivate()) {
			setPrivate();
		} else if (m.isProtected()) {
			setProtected();
		}
	}

	public ModifierModel copyOf() {
		return new ModifierModel(mods);
	}

	public boolean isPackage() {
		return !isPrivate() && !isPublic() && !isProtected();
	}

	public void setPackage() {
		setAccess(0);
	}

	public boolean isPublic() {
		return Modifier.isPublic(mods);
	}

	public void setPublic() {
		setAccess(Modifier.PUBLIC);
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(mods);
	}

	public void setPrivate() {
		setAccess(Modifier.PRIVATE);
	}

	public boolean isProtected() {
		return Modifier.isProtected(mods);
	}

	public void setProtected() {
		setAccess(Modifier.PROTECTED);
	}

	private void setAccess(int mod) {
		mods &= ~Modifier.PRIVATE;
		mods &= ~Modifier.PROTECTED;
		mods &= ~Modifier.PUBLIC;

		mod |= mod;
	}

	public boolean isStatic() {
		return Modifier.isStatic(mods);
	}

	public void setStatic(boolean isStatic) {
		set(Modifier.STATIC, isStatic);
	}

	public boolean isNative() {
		return Modifier.isNative(mods);
	}

	public void setNative(boolean isNative) {
		set(Modifier.NATIVE, isNative);
	}

	public boolean isFinal() {
		return Modifier.isFinal(mods);
	}

	public void setFinal(boolean isFinal) {
		set(Modifier.FINAL, isFinal);
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(mods);
	}

	public void setAbstract(boolean isAbstract) {
		set(Modifier.ABSTRACT, isAbstract);
	}

	public boolean isSynchronized() {
		return Modifier.isSynchronized(mods);
	}

	public void setSynchronized(boolean isSynchronized) {
		set(Modifier.SYNCHRONIZED, isSynchronized);
	}

	public boolean isTransient() {
		return Modifier.isTransient(mods);
	}

	public void setTransient(boolean isTransient) {
		set(Modifier.TRANSIENT, isTransient);
	}

	public boolean isVolatile() {
		return Modifier.isVolatile(mods);
	}

	public void setVolatile(boolean isVolatile) {
		set(Modifier.VOLATILE, isVolatile);
	}

	public boolean isStict() {
		return Modifier.isStrict(mods);
	}

	public void setStrict(boolean isStrict) {
		set(Modifier.STRICT, isStrict);
	}

	private void set(int mod, boolean enable) {
		if (enable) {
			mods |= mod;
		} else {
			mods &= ~mod;
		}
	}

}
