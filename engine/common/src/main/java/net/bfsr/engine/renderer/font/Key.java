package net.bfsr.engine.renderer.font;

public class Key {
    /**
     * A copy of the String which this Key is indexing. A copy is used to avoid creating a strong reference to the original
     * passed into renderString(). When the original String is no longer needed by Minecraft, it will be garbage collected
     * and the WeakHashMaps in StringCache will allow this Key object and its associated Entry object to be garbage
     * collected as well.
     */
    public String str;

    public int fontSize;

    /**
     * Computes a hash code on str in the same manner as the String class, except all ASCII digits hash as '0'
     *
     * @return the augmented hash code on str
     */
    @Override
    public int hashCode() {
        int code = 0, length = str.length();

        for (int index = 0; index < length; index++) {
            code = (code * 31) + str.charAt(index);
        }

        code = (code * 31) + fontSize;

        return code;
    }

    /**
     * Compare str against another object (specifically, the object's string representation as returned by toString).
     * All ASCII digits are considered equal by this method, as long as they are at the same index within the string.
     *
     * @return true if the strings are the identical, or only differ in their ASCII digits
     */
    @Override
    public boolean equals(Object o) {
        /*
         * There seems to be a timing window inside WeakHashMap itself where a null object can be passed to this
         * equals() method. Presumably it happens between computing a hash code for the weakly referenced Key object
         * while it still exists and calling its equals() method after it was garbage collected.
         */
        if (o == null) {
            return false;
        }

        /* Calling toString on a String object simply returns itself so no new object allocation is performed */
        String other = o.toString();
        int length = str.length();

        if (length != other.length()) {
            return false;
        }

        if (fontSize != ((Key) o).fontSize) {
            return false;
        }

        /*
         * True if a section mark character was last seen. In this case, if the next character is a digit, it must
         * not be considered equal to any other digit. This forces any string that differs in color codes only to
         * have a separate entry in the StringCache.
         */
        boolean colorCode = false;

        for (int index = 0; index < length; index++) {
            char c1 = str.charAt(index);
            char c2 = other.charAt(index);

            if (c1 != c2 && colorCode) {
                return false;
            }
            colorCode = (c1 == '§');
        }

        return true;
    }

    /**
     * Returns the contained String object within this Key.
     *
     * @return the str object
     */
    @Override
    public String toString() {
        return str;
    }
}