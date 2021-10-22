package team.unnamed.emojis.io;

import java.io.IOException;

/**
 * Implementation of {@link AssetWriter} that
 * writes the resource pack information.
 * @see ResourcePackInfo
 */
public class ResourcePackInfoWriter
        implements AssetWriter {

    private final ResourcePackInfo info;

    public ResourcePackInfoWriter(ResourcePackInfo info) {
        this.info = info;
    }

    @Override
    public void write(TreeOutputStream output) throws IOException {
        // write the pack data
        output.useEntry("pack.mcmeta");
        Streams.writeUTF(
                output,
                "{ " +
                        "\"pack\":{" +
                        "\"pack_format\":" + info.getFormat() + "," +
                        "\"description\":\"" + info.getDescription() + "\"" +
                        "}" +
                        "}"
        );
        output.closeEntry();

        // write the pack icon if not null
        Writeable icon = info.getIcon();
        if (icon != null) {
            output.useEntry("pack.png");
            icon.write(output);
            output.closeEntry();
        }
    }

}