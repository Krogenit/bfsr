package net.bfsr.engine.renderer.font.legacy;

import org.joml.Vector3f;

import java.awt.Font;
import java.awt.Point;
import java.awt.font.GlyphVector;
import java.lang.ref.WeakReference;
import java.text.Bidi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

/**
 * The StringCache is the public interface for rendering of all Unicode strings using OpenType fonts. It caches the glyph layout
 * of individual strings, and it uses a GlyphCache instance to cache the pre-rendered images for individual glyphs. Once a string
 * and its glyph images are cached, the critical path in renderString() will draw the glyphs as fast as if using a bitmap font.
 * Strings are cached using weak references through a two layer string cache. Strings that are no longer in use by Minecraft will
 * be evicted from the cache, while the pre-rendered images of individual glyphs remains cached forever. The following diagram
 * illustrates how this works:
 * <pre>
 * String passed to Key object considers Entry object holds Each Glyph object GlyphCache.Entry stores
 * renderString(); all ASCII digits equal an array of Glyph belongs to only one the texture ID, image
 * mapped by weak to zero ('0'); objects which may Entry object; it has width/height and
 * weakRefCache mapped by weak not directly the glyph x/y pos normalized texture
 * to Key object stringCahe to Entry correspond to Unicode within the string coordinates.
 * chars in string
 * String("Fi1") ------------\ ---> Glyph("F") ----------> GlyphCache.Entry("F")
 * N:1 \ 1:1 1:N / N:1
 * String("Fi4") ------------> Key("Fi0") -------------> Entry("Fi0") -----+----> Glyph("i") ----------> GlyphCache.Entry("i")
 * \ N:1
 * ---> Glyph("0") -----\
 * ----> GlyphCache.Entry("0")
 * ---> Glyph("0") -----/
 * N:1 1:1 1:N / N:1
 * String("Be1") ------------> Key("Be0") -------------> Entry("Be0") -----+----> Glyph("e") ----------> GlyphCache.Entry("e")
 * \ N:1
 * ---> Glyph("B") ----------> GlyphCache.Entry("B")
 * </pre>
 */
public class StringCache {
    private static final Vector3f[] COLOR_TABLE = new Vector3f[32];
    private static final char SPACE = ' ';
    private static final char NEW_LINE = '\n';

    static {
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            COLOR_TABLE[i] = new Vector3f(k / 255.0f, l / 255.0f, i1 / 255.0f);
        }
    }

    /**
     * Reference to the unicode.FontRenderer class. Needed for creating GlyphVectors and retrieving glyph texture coordinates.
     */
    private final GlyphCache glyphCache;

    /**
     * A cache of recently seen strings to their fully layed-out state, complete with color changes and texture coordinates of
     * all pre-rendered glyph images needed to display this string. The weakRefCache holds strong references to the Key
     * objects used in this map.
     */
    private final WeakHashMap<Key, Entry> stringCache = new WeakHashMap<>();

    /**
     * Temporary Key object re-used for lookups with stringCache.get(). Using a temporary object like this avoids the overhead
     * of allocating new objects in the critical rendering path. Of course, new Key objects are always created when adding
     * a mapping to stringCache.
     */
    private final Key lookupKey = new Key();

    StringCache(String fontFileName, boolean antiAlias) {
        glyphCache = new GlyphCache();

        setFontFromFile(fontFileName, antiAlias);
    }

    private void setFontFromFile(String fontFileName, boolean antiAlias) {
        /* Change the font in the glyph cache and clear the string cache so all strings have to be re-layed out and re-rendered */
        glyphCache.setFontFromFile(fontFileName, antiAlias);
        stringCache.clear();
    }

    /**
     * Add a string to the string cache by perform full layout on it, remembering its glyph positions, and making sure that
     * every font glyph used by the string is pre-rendering. If this string has already been cached, then simply return its
     * existing Entry from the cahe. Note that for caching purposes, this method considers two strings to be identical if they
     * only differ in their ASCII digits; the renderString() method performs fast glyph substitution based on the actual digits
     * in the string at the time.
     *
     * @param str this String will be layed out and added to the cache (or looked up, if alraedy cached)
     * @return the string's cache entry containing all the glyph positions
     */
    Entry cacheString(String str, int fontSize) {
        /*
         * New Key object allocated only if the string was not found in the StringCache using lookupKey. This variable must
         * be outside the (entry == null) code block to have a temporary strong reference between the time when the Key is
         * added to stringCache and added to weakRefCache.
         */
        Key key;
        fontSize = fontSize << 1;

        /* Re-use existing lookupKey to avoid allocation overhead on the critical rendering path */
        lookupKey.str = str;
        lookupKey.fontSize = fontSize;

        /* If this string is already in the cache, simply return the cached Entry object */
        Entry entry = stringCache.get(lookupKey);

        /* If string is not cached (or not on main thread) then layout the string */
        if (entry == null) {
            /* layoutGlyphVector() requires a char[] so create it here and pass it around to avoid duplication later on */
            char[] text = str.toCharArray();

            /* Strip all color codes from the string */
            entry = new Entry();
            int length = text.length;

            /* Layout the entire string, splitting it up by color codes and the Unicode bidirectional algorithm */
            List<Glyph> glyphList = new ArrayList<>();
            entry.advance = layoutBidiString(glyphList, text, 0, length, fontSize);

            /* Convert the accumulated Glyph list to an array for efficient storage */
            entry.glyphs = new Glyph[glyphList.size()];
            entry.glyphs = glyphList.toArray(entry.glyphs);

            /*
             * Sort Glyph array by stringIndex so it can be compared during rendering to the already sorted ColorCode array.
             * This will apply color codes in the string's logical character order and not the visual order on screen.
             */
            Arrays.sort(entry.glyphs);

            /* Wrap the string in a Key object (to change how ASCII digits are compared) and cache it along with the newly generated Entry */
            key = new Key();

            /* Make a copy of the original String to avoid creating a strong reference to it */
            key.str = str;
            key.fontSize = fontSize;
            entry.keyRef = new WeakReference<>(key);
            stringCache.put(key, entry);
        }

        lookupKey.str = null;
        lookupKey.fontSize = 0;

        /* Return either the existing or the newly created entry so it can be accessed immediately */
        return entry;
    }

    /**
     * Split a string into contiguous LTR or RTL sections by applying the Unicode Bidirectional Algorithm. Calls layoutString()
     * for each contiguous run to perform further analysis.
     *
     * @param glyphList will hold all new Glyph objects allocated by layoutFont()
     * @param text      the string to lay out
     * @param start     the offset into text at which to start the layout
     * @param limit     the (offset + length) at which to stop performing the layout
     * @return the total advance (horizontal distance) of this string
     */
    private int layoutBidiString(List<Glyph> glyphList, char[] text, int start, int limit, int fontSize) {
        int advance = 0;

        /* Avoid performing full bidirectional analysis if text has no "strong" right-to-left characters */
        if (Bidi.requiresBidi(text, start, limit)) {
            /* Note that while requiresBidi() uses start/limit the Bidi constructor uses start/length */
            Bidi bidi = new Bidi(text, start, null, 0, limit - start, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);

            /* If text is entirely right-to-left, then insert an EntryText node for the entire string */
            if (bidi.isRightToLeft()) {
                return layoutStyle(glyphList, text, start, limit, Font.LAYOUT_RIGHT_TO_LEFT, advance, fontSize);
            }

            /* Otherwise text has a mixture of LTR and RLT, and it requires full bidirectional analysis */
            else {
                int runCount = bidi.getRunCount();
                byte[] levels = new byte[runCount];
                Integer[] ranges = new Integer[runCount];

                /* Reorder contiguous runs of text into their display order from left to right */
                for (int index = 0; index < runCount; index++) {
                    levels[index] = (byte) bidi.getRunLevel(index);
                    ranges[index] = index;
                }
                Bidi.reorderVisually(levels, 0, ranges, 0, runCount);

                /*
                 * Every GlyphVector must be created on a contiguous run of left-to-right or right-to-left text. Keep track of
                 * the horizontal advance between each run of text, so that the glyphs in each run can be assigned a position relative
                 * to the start of the entire string and not just relative to that run.
                 */
                for (int visualIndex = 0; visualIndex < runCount; visualIndex++) {
                    int logicalIndex = ranges[visualIndex];

                    /* An odd numbered level indicates right-to-left ordering */
                    int layoutFlag = (bidi.getRunLevel(logicalIndex) & 1) == 1 ? Font.LAYOUT_RIGHT_TO_LEFT : Font.LAYOUT_LEFT_TO_RIGHT;
                    advance = layoutStyle(glyphList, text, start + bidi.getRunStart(logicalIndex), start + bidi.getRunLimit(logicalIndex),
                            layoutFlag, advance, fontSize);
                }
            }

            return advance;
        }

        /* If text is entirely left-to-right, then insert an EntryText node for the entire string */
        else {
            return layoutStyle(glyphList, text, start, limit, Font.LAYOUT_LEFT_TO_RIGHT, advance, fontSize);
        }
    }

    private int layoutStyle(List<Glyph> glyphList, char[] text, int start, int limit, int layoutFlags, int advance, int fontSize) {
        /* Break up the string into segments, where each segment has the same font style in use */
        while (start < limit) {
            /* Layout the string segment with the style currently selected by the last color code */
            advance = layoutString(glyphList, text, start, limit, layoutFlags, advance, Font.PLAIN, fontSize);
            start = limit;
        }

        return advance;
    }

    /**
     * Given a string that runs contiguously LTR or RTL, break it up into individual segments based on which fonts can render
     * which characters in the string. Calls layoutFont() for each portion of the string that can be layed out with a single
     * font.
     *
     * @param glyphList   will hold all new Glyph objects allocated by layoutFont()
     * @param text        the string to lay out
     * @param start       the offset into text at which to start the layout
     * @param limit       the (offset + length) at which to stop performing the layout
     * @param layoutFlags either Font.LAYOUT_RIGHT_TO_LEFT or Font.LAYOUT_LEFT_TO_RIGHT
     * @param advance     the horizontal advance (i.e. X position) returned by previous call to layoutString()
     * @param style       combination of Font.PLAIN, Font.BOLD, and Font.ITALIC to select a fonts with some specific style
     * @return the advance (horizontal distance) of this string plus the advance passed in as an argument
     * todo Correctly handling RTL font selection requires scanning the sctring from RTL as well.
     * todo Use bitmap fonts as a fallback if no OpenType font could be found
     */
    private int layoutString(List<Glyph> glyphList, char[] text, int start, int limit, int layoutFlags, int advance, int style,
                             int fontSize) {
        /* Break the string up into segments, where each segment can be displayed using a single font */
        while (start < limit) {
            Font font = glyphCache.lookupFont(text, start, limit, style, fontSize);
            int next = font.canDisplayUpTo(text, start, limit);

            /* canDisplayUpTo returns -1 if the entire string range is supported by this font */
            if (next == -1) {
                next = limit;
            }

            /*
             * canDisplayUpTo() returns start if the starting character is not supported at all. In that case, draw just the
             * one unsupported character (which will use the font's "missing glyph code"), then retry the lookup again at the
             * next character after that.
             */
            if (next == start) {
                next++;
            }

            advance = layoutFont(glyphList, text, start, next, layoutFlags, advance, font);
            start = next;
        }

        return advance;
    }

    /**
     * Allocate new Glyph objects and add them to the glyph list. This sequence of Glyphs represents a portion of the
     * string where all glyphs run contiguously in either LTR or RTL and come from the same physical/logical font.
     *
     * @param glyphList   all newly created Glyph objects are added to this list
     * @param text        the string to layout
     * @param start       the offset into text at which to start the layout
     * @param limit       the (offset + length) at which to stop performing the layout
     * @param layoutFlags either Font.LAYOUT_RIGHT_TO_LEFT or Font.LAYOUT_LEFT_TO_RIGHT
     * @param advance     the horizontal advance (i.e. X position) returned by previous call to layoutString()
     * @param font        the Font used to layout a GlyphVector for the string
     * @return the advance (horizontal distance) of this string plus the advance passed in as an argument
     * todo need to ajust position of all glyphs if digits are present, by assuming every digit should be 0 in length
     */
    private int layoutFont(List<Glyph> glyphList, char[] text, int start, int limit, int layoutFlags, int advance, Font font) {
        /*
         * Ensure that all glyphs used by the string are pre-rendered and cached in the texture. Only safe to do so from the
         * main thread because cacheGlyphs() can crash LWJGL if it makes OpenGL calls from any other thread. In this case,
         * cacheString() will also not insert the entry into the stringCache since it may be incomplete if lookupGlyph()
         * returns null for any glyphs not yet stored in the glyph cache.
         */
        glyphCache.cacheGlyphs(font, text, start, limit, layoutFlags);

        /* Creating a GlyphVector takes care of all language specific OpenType glyph substitutions and positionings */
        GlyphVector vector = glyphCache.layoutGlyphVector(font, text, start, limit, layoutFlags);

        /*
         * Extract all needed information for each glyph from the GlyphVector so it won't be needed for actual rendering.
         * Note that initially, glyph.start holds the character index into the stripped text array. But after the entire
         * string is layed out, this field will be adjusted on every Glyph object to correctly index the original unstripped
         * string.
         */
        Glyph glyph = null;
        int numGlyphs = vector.getNumGlyphs();
        for (int index = 0; index < numGlyphs; index++) {
            Point position = vector.getGlyphPixelBounds(index, null, advance, 0).getLocation();
            /* Compute horizontal advance for the previous glyph based on this glyph's position */
            if (glyph != null) {
                glyph.advance = position.x - glyph.x;
            }

            /*
             * Allocate a new glyph object and add to the glyphList. The glyph.stringIndex here is really like stripIndex but
             * it will be corrected later to account for the color codes that have been stripped out.
             */
            glyph = new Glyph();
            glyph.stringIndex = start + vector.getGlyphCharIndex(index);
            glyph.texture = glyphCache.lookupGlyph(font, vector.getGlyphCode(index));
            glyph.x = position.x;
            glyph.y = position.y;
            glyphList.add(glyph);
        }

        /* Compute the advance position of the last glyph (or only glyph) since it can't be done by the above loop */
        advance = (int) (advance + vector.getGlyphPosition(numGlyphs).getX());

        if (glyph != null) {
            glyph.advance = advance - glyph.x;
        }

        /* Return the overall horizontal advance in pixels from the start of string */
        return advance;
    }

    /**
     * Return the width of a string in pixels. Used for centering strings inside GUI buttons.
     *
     * @param str compute the width of this string
     * @return the width in pixels (divided by 2; this matches the scaled coordinate system used by GUIs in Minecraft)
     */
    @SuppressWarnings("unused")
    public int getWidth(String str, int fontSize) {
        /* Check for invalid arguments */
        if (str == null || str.isEmpty()) {
            return 0;
        }

        /* Make sure the entire string is cached and rendered since it will probably be used again in a renderString() call */
        Entry entry = cacheString(str, fontSize);

        /* Return total horizontal advance (slightly wider than the bounding box, but close enough for centering strings) */
        return entry.advance / 2;
    }

    public int getCursorPositionInLine(String str, float mouseX, int fontSize) {
        /* Check for invalid arguments */
        if (str.isEmpty()) {
            return 0;
        }

        mouseX += mouseX;

        /* The glyph array for a string is sorted by the string's logical character position */
        Glyph[] glyphs = cacheString(str, fontSize).glyphs;

        /* Add up the individual advance of each glyph until it exceeds the specified width */
        float advance = 0.0f;
        int index = 0;
        while (index < glyphs.length && advance <= mouseX) {
            float halfAdvance = glyphs[index].advance / 2.0f;
            float nextAdvance = advance + halfAdvance;
            if (nextAdvance <= mouseX) {
                advance = nextAdvance + halfAdvance;
                index++;
            } else {
                break;
            }
        }

        /* The string index of the last glyph that wouldn't fit gives the total desired length of the string in characters */
        return index < glyphs.length ? glyphs[index].stringIndex : str.length();
    }

    /**
     * Return the number of characters in a string that will completly fit inside the specified width when rendered.
     *
     * @param str   the String to analyze
     * @param width the desired string width (in GUI coordinate system)
     * @return the number of characters from str that will fit inside width
     */
    @SuppressWarnings("unused")
    public int sizeStringToWidth(String str, int fontSie, int width) {
        return getWidth(str, fontSie, width, true);
    }

    /**
     * Trim a string so that it fits in the specified width when rendered, optionally reversing the string
     *
     * @param str     the String to trim
     * @param width   the desired string width (in GUI coordinate system)
     * @param reverse if true, the returned string will also be reversed
     * @return the trimmed and optionally reversed string
     */
    @SuppressWarnings("unused")
    public String trimStringToWidth(String str, int fontSize, int width, boolean reverse) {
        if (reverse)
            str = new StringBuilder(str).reverse().toString();

        int length = getWidth(str, fontSize, width, true);
        str = str.substring(0, length);

        if (reverse) {
            str = (new StringBuilder(str)).reverse().toString();
        }

        return str;
    }

    String trimStringToWidthSaveWords(String string, int fontSize, int width) {
        return string.substring(0, getWidth(string, fontSize, width, true));
    }

    public Vector3f getColor(int colorCode) {
        return COLOR_TABLE[colorCode];
    }

    /**
     * Return the number of characters in a string that will completly fit inside the specified width when rendered, with
     * or without prefering to break the line at whitespace instead of breaking in the middle of a word. This private provides
     * the real implementation of both sizeStringToWidth() and trimStringToWidth().
     *
     * @param str           the String to analyze
     * @param width         the desired string width (in GUI coordinate system)
     * @param breakAtSpaces set to prefer breaking line at spaces than in the middle of a word
     * @return the number of characters from str that will fit inside width
     */
    public int getWidth(String str, int fontSize, int width, boolean breakAtSpaces) {
        /* Check for invalid arguments */
        if (str == null || str.isEmpty()) {
            return 0;
        }

        width += width;

        /* The glyph array for a string is sorted by the string's logical character position */
        Glyph[] glyphs = cacheString(str, fontSize).glyphs;

        /* Index of the last whitespace found in the string; used if breakAtSpaces is true */
        int wsIndex = -1;

        /* Add up the individual advance of each glyph until it exceeds the specified width */
        int advance = 0, index = 0;
        while (index < glyphs.length && advance <= width) {
            /* Keep track of spaces if breakAtSpaces it set */
            if (breakAtSpaces) {
                char c = str.charAt(glyphs[index].stringIndex);
                if (c == SPACE) {
                    wsIndex = index + 1;
                } else if (c == NEW_LINE) {
                    wsIndex = index + 1;
                    break;
                }
            }

            int nextAdvance = advance + glyphs[index].advance;
            if (nextAdvance <= width) {
                advance = nextAdvance;
                index++;
            } else {
                break;
            }
        }

        /* Avoid splitting individual words if breakAtSpaces set; same test condition as in Minecraft's FontRenderer */
        if (index < glyphs.length && wsIndex >= 0) {
            index = wsIndex;
        }

        /* The string index of the last glyph that wouldn't fit gives the total desired length of the string in characters */
        return index < glyphs.length ? glyphs[index].stringIndex : str.length();
    }

    public int getHeight(String text, int fontSize, int maxWidth, int indent) {
        int offset = 0;
        int height = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == NEW_LINE) {
                height = (int) (height + getHeight("\n", fontSize) * indent);
            }
        }
        return height + getTrimmedStringHeight(text.substring(offset).trim(), fontSize, maxWidth, indent);
    }

    public float getHeight(String s, int fontSize) {
        return glyphCache.getHeight(s, fontSize);
    }

    public float getAscent(String s, int fontSize) {
        return glyphCache.getAscent(s, fontSize);
    }

    public float getDescent(String s, int fontSize) {
        return glyphCache.getDescent(s, fontSize);
    }

    public float getLeading(String s, int fontSize) {
        return glyphCache.getLeading(s, fontSize);
    }

    private int getTrimmedStringHeight(String string, int fontSize, int maxWidth, int indent) {
        int height = 0;
        do {
            String temp = trimStringToWidthSaveWords(string, fontSize, maxWidth);
            string = string.replace(temp, "").trim();
            height = (int) (height + (getHeight(temp, fontSize) + indent));
        } while (!string.isEmpty());

        return height;
    }

    public int getCenteredOffsetY(String string, int height, int fontSize) {
        float v = height - getAscent(string, fontSize);
        return Math.round(v / 2.0f);
    }
}