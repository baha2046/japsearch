package org.nagoya.system.event;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class CustomEvent {
    public final Object object;
    public final CustomEventType type;

    @Contract(pure = true)
    @java.beans.ConstructorProperties({"object", "type"})
    public CustomEvent(Object object, CustomEventType type) {
        this.object = object;
        this.type = type;
    }

    public Object getObject() {
        return this.object;
    }

    public CustomEventType getType() {
        return this.type;
    }

    @Contract(value = "null -> false", pure = true)
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CustomEvent)) return false;
        final CustomEvent other = (CustomEvent) o;
        if (!other.canEqual((Object) this)) return false;
        if (!Objects.equals(this.object, other.object)) return false;
        if (!Objects.equals(this.type, other.type)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CustomEvent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $object = this.object;
        result = result * PRIME + ($object == null ? 43 : $object.hashCode());
        final Object $type = this.type;
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    public String toString() {
        return "CustomEvent(object=" + this.object + ", type=" + this.type + ")";
    }
}
