package org.nagoya.model.dataitem;

public abstract class Person extends MovieDataItem {
    private static final long serialVersionUID = -3281087566864375512L;
    private String name;
    private FxThumb thumb;
    private boolean thumbEdited; //did we change the URL of the thumb since loading and thus need to force a refresh

    public Person(String name, FxThumb thumb) {
        this.setName(name);
        this.thumb = thumb;
        this.thumbEdited = false;
    }

    public Person() {
        this.name = "";
        this.thumb = null;
        this.thumbEdited = false;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = sanitizeString(name);
    }

    public FxThumb getThumb() {
        return this.thumb;
    }

    public void setThumb(FxThumb thumb) {
        this.thumb = thumb;
    }

    @Override
    abstract public String toXML();

    @Override
    public String toString() {
        return "Person [name=\"" + this.name + "\", thumb=" + this.thumb + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.thumb == null) ? 0 : this.thumb.hashCode());
        result = prime * result + (this.thumbEdited ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Person other = (Person) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.thumb == null) {
            if (other.thumb != null) {
                return false;
            }
        } else if (!this.thumb.equals(other.thumb)) {
            return false;
        }
        if (this.thumbEdited != other.thumbEdited) {
            return false;
        }
        return true;
    }

    public boolean isThumbEdited() {
        return this.thumbEdited;
    }

    public void setThumbEdited(boolean thumbEdited) {
        this.thumbEdited = thumbEdited;
    }

}
