package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Layout;
import com.ariasaproject.advancerofrpg.utils.Null;

abstract public class Value {
	static public final Fixed zero = new Fixed(0);
	static public Value minWidth = new Value() {
		@Override
		public float get(@Null Actor context) {
			if (context instanceof Layout)
				return ((Layout) context).getMinWidth();
			return context == null ? 0 : context.getWidth();
		}
	};
	static public Value minHeight = new Value() {
		@Override
		public float get(@Null Actor context) {
			if (context instanceof Layout)
				return ((Layout) context).getMinHeight();
			return context == null ? 0 : context.getHeight();
		}
	};
	static public Value prefWidth = new Value() {
		@Override
		public float get(@Null Actor context) {
			if (context instanceof Layout)
				return ((Layout) context).getPrefWidth();
			return context == null ? 0 : context.getWidth();

		}
	};
	static public Value prefHeight = new Value() {
		@Override
		public float get(@Null Actor context) {
			if (context instanceof Layout)
				return ((Layout) context).getPrefHeight();
			return context == null ? 0 : context.getHeight();
		}
	};
	static public Value maxWidth = new Value() {
		@Override
		public float get(@Null Actor context) {
			if (context instanceof Layout)
				return ((Layout) context).getMaxWidth();
			return context == null ? 0 : context.getWidth();
		}
	};
	static public Value maxHeight = new Value() {
		@Override
		public float get(@Null Actor context) {
			if (context instanceof Layout)
				return ((Layout) context).getMaxHeight();
			return context == null ? 0 : context.getHeight();
		}
	};

	static public Value percentWidth(final float percent) {
		return new Value() {
			@Override
			public float get(@Null Actor actor) {
				return actor.getWidth() * percent;
			}
		};
	}

	static public Value percentHeight(final float percent) {
		return new Value() {
			@Override
			public float get(@Null Actor actor) {
				return actor.getHeight() * percent;
			}
		};
	}

	static public Value percentWidth(final float percent, final Actor actor) {
		if (actor == null)
			throw new IllegalArgumentException("actor cannot be null.");
		return new Value() {
			@Override
			public float get(@Null Actor context) {
				return actor.getWidth() * percent;
			}
		};
	}

	static public Value percentHeight(final float percent, final Actor actor) {
		if (actor == null)
			throw new IllegalArgumentException("actor cannot be null.");
		return new Value() {
			@Override
			public float get(@Null Actor context) {
				return actor.getHeight() * percent;
			}
		};
	}

	public float get() {
		return get(null);
	}

	abstract public float get(@Null Actor context);

	static public class Fixed extends Value {
		static final Fixed[] cache = new Fixed[111];
		private final float value;

		public Fixed(float value) {
			this.value = value;
		}

		static public Fixed valueOf(float value) {
			if (value == 0)
				return zero;
			if (value >= -10 && value <= 100 && value == (int) value) {
				Fixed fixed = cache[(int) value + 10];
				if (fixed == null)
					cache[(int) value + 10] = fixed = new Fixed(value);
				return fixed;
			}
			return new Fixed(value);
		}

		@Override
		public float get(@Null Actor context) {
			return value;
		}

		@Override
		public String toString() {
			return Float.toString(value);
		}
	}
}
