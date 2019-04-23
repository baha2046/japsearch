package org.nagoya.system.event;

import org.jetbrains.annotations.Contract;

import java.beans.ConstructorProperties;
import java.util.Objects;

public class CustomEventType {
    private final String name;

    @Contract(pure = true)
    @ConstructorProperties({"name"})
    public CustomEventType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Contract(value = "null -> false", pure = true)
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CustomEventType)) return false;
        final CustomEventType other = (CustomEventType) o;
        if (!other.canEqual((Object) this)) return false;
        return Objects.equals(this.name, other.name);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CustomEventType;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $name = this.name;
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        return result;
    }

    public String toString() {
        return "CustomEventType(name=" + this.name + ")";
    }
}
