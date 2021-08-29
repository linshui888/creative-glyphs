package team.unnamed.emojis.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for replacing emojis and obtaining
 * the result in a rich component like {@link BaseComponent[]}
 */
public final class ComponentEmojiReplacer {

    /**
     * Pattern for matching emojis from a string
     */
    private static final Pattern EMOJI_PATTERN
            = Pattern.compile(":([A-Za-z_]{1,14}):");

    /**
     * Pattern for matching URLs
     */
    private static final Pattern URL_PATTERN
            = Pattern.compile( "^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$" );

    // convenience constant holding an empty component array
    private static final BaseComponent[] EMPTY_COMPONENT_ARRAY
            = new BaseComponent[0];

    private ComponentEmojiReplacer() {
    }

    private static void fromLegacyText(
            String message,
            List<TextComponent> components,
            TextComponent component
    ) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = URL_PATTERN.matcher(message);

        for (int i = 0; i < message.length(); i++) {
            char current = message.charAt(i);
            if (current == ChatColor.COLOR_CHAR) {
                if (++i >= message.length()) {
                    break;
                }

                current = message.charAt(i);

                // basically, a toLowerCase()
                if (current >= 'A' && current <= 'Z') {
                    current += 32;
                }

                ChatColor format;
                if (current == 'x' && i + 12 < message.length()) {
                    StringBuilder hex = new StringBuilder("#");
                    for (int j = 0; j < 6; j++) {
                        hex.append(message.charAt(i + 2 + (j * 2)));
                    }
                    try {
                        format = ChatColor.of(hex.toString());
                    } catch (IllegalArgumentException ex) {
                        format = null;
                    }

                    i += 12;
                } else {
                    format = ChatColor.getByChar(current);
                }

                if (format == null) {
                    continue;
                }

                if (builder.length() > 0) {
                    TextComponent old = component;
                    component = new TextComponent(old);
                    old.setText(builder.toString());
                    builder = new StringBuilder();
                    components.add(old);
                }
                if (format == ChatColor.BOLD) {
                    component.setBold(true);
                } else if (format == ChatColor.ITALIC) {
                    component.setItalic(true);
                } else if (format == ChatColor.UNDERLINE) {
                    component.setUnderlined(true);
                } else if (format == ChatColor.STRIKETHROUGH) {
                    component.setStrikethrough(true);
                } else if (format == ChatColor.MAGIC) {
                    component.setObfuscated(true);
                } else if (format == ChatColor.RESET) {
                    format = ChatColor.GRAY;
                    component = new TextComponent();
                    component.setColor(format);
                } else {
                    component = new TextComponent();
                    component.setColor(format);
                }
                continue;
            }

            int pos = message.indexOf(' ', i);
            if (pos == -1) {
                pos = message.length();
            }
            if (matcher.region(i, pos).find()) { //Web link handling
                if (builder.length() > 0) {
                    TextComponent old = component;
                    component = new TextComponent(old);
                    old.setText(builder.toString());
                    builder = new StringBuilder();
                    components.add(old);
                }

                TextComponent old = component;
                component = new TextComponent(old);
                String urlString = message.substring(i, pos);
                component.setText(urlString);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        urlString.startsWith( "http" ) ? urlString : "https://" + urlString ) );
                components.add(component);
                i += pos - i - 1;
                component = old;
                continue;
            }
            builder.append(current);
        }

        component.setText(builder.toString());
        components.add(component);
    }

    public static BaseComponent[] replace(
            Permissible permissible,
            EmojiRegistry registry,
            String message,
            BiConsumer<List<TextComponent>, Emoji> buildEmojiComponent
    ) {
        List<TextComponent> components = new ArrayList<>();
        Matcher matcher = EMOJI_PATTERN.matcher(message);
        int lastEnd = 0;

        TextComponent last = new TextComponent();

        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);

            if (start - lastEnd > 0) {
                // so there's text within this emoji and the previous emoji (or text start)
                String previous = message.substring(lastEnd, start - 1);
                fromLegacyText(previous, components, last);
                last = components.get(components.size() - 1).duplicate();
            }

            String emojiName = message.substring(start, end);
            Emoji emoji = registry.get(emojiName);

            if (emoji == null || !permissible.hasPermission(emoji.getPermission())) {
                // if invalid emoji, lastEnd is the current start - 1, so it
                // consumes the emoji and its starting colon for the next
                // "previous" text
                lastEnd = start - 1;
            } else {
                buildEmojiComponent.accept(components, emoji);
                // if valid emoji, lastEnd is the emoji end + 1, so it doesn't
                // consume the emoji nor its closing colon
                lastEnd = end + 1;
            }
        }

        // append remaining text
        if (message.length() - lastEnd > 0) {
            fromLegacyText(message.substring(lastEnd), components, last);
        }

        return components.toArray(EMPTY_COMPONENT_ARRAY);
    }

}