package org.nagoya.preferences;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import io.vavr.collection.Stream;
import org.nagoya.model.MovieV2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class RenameSettings extends Settings {
    private static final String fileName = "rename.xml";

    private static RenameSettings INSTANCE = null;

    private String[] company;
    private String[] renameDirectoryFormat;
    private String[] renameFileFormat;

    private RenameSettings() {
        //prevent people from using this
        this.company = new String[2];
        this.company[0] = "TEST1|TEST1";
        this.renameDirectoryFormat = new String[]{"#date", "space", "[", "#id", "]", "space", "#moviename"};
        this.renameFileFormat = new String[]{"[", "#id", "]"};
    }

    public static synchronized RenameSettings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = readSetting();
        }
        return INSTANCE;
    }

    private static RenameSettings readSetting() {
        try {
            Path path = Paths.get(fileName);

            String targetFileStr = (Files.lines(path)).collect(Collectors.joining("\n"));

            XStream xstream = getXMLSerializer();
            xstream.ignoreUnknownElements();

            return (RenameSettings) xstream.fromXML(targetFileStr);
        } catch (Exception e) {

            System.out.println("rename.xml cannot be read in.");
        }

        return new RenameSettings();
    }

    private static XStream getXMLSerializer() {
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.allowTypeHierarchy(Collection.class);

        //xstream.addImplicitArray(RenameSettings.class, "company");
        return xstream;
    }

    public static String getSuitableDirectoryName(MovieV2 movieV2) {
        StringBuilder stringBuilder = new StringBuilder();

        Stream.of(getInstance().getRenameDirectoryFormat())
                .map((s) -> MovieV2.getFormatUnit(movieV2, s))
                .forEach(stringBuilder::append);

        return stringBuilder.toString();

    }

    public static String getSuitableFileName(MovieV2 movieV2) {
        StringBuilder stringBuilder = new StringBuilder();

        Stream.of(getInstance().getRenameFileFormat())
                .map((s) -> MovieV2.getFormatUnit(movieV2, s))
                .forEach(stringBuilder::append);

        return stringBuilder.toString();
    }



    public String renameCompany(String inString) {
        return Stream.of(this.company).filter(s -> s.substring(0, s.indexOf("|")).equals(inString))
                .map(s -> s.substring(s.indexOf("|") + 1)).getOrElse(inString);
    }

    public void updateRenameMapping(String inString)
    {
        String[] strings = getCompany();
        boolean isUpdated = false;
        for(int x=0; x<strings.length; x++)
        {
            if(inString.substring(0,inString.indexOf("|")).equals(strings[x].substring(0, strings[x].indexOf("|"))))
            {
                strings[x] = inString;
                isUpdated = true;
                break;
            }
        }
        if(!isUpdated)
        {
            String[] newStrings = new String[strings.length + 1];
            System.arraycopy(strings,0 , newStrings ,0 , strings.length);
            newStrings[newStrings.length - 1] = inString;
            setCompany(newStrings);
        }
    }

    public void writeSetting() {
        String xml = getXMLSerializer().toXML(this);

        // add the xml header since xstream doesn't do this
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" + "\n" + xml;
        //system.out.println("Xml I am writing to file: \n" + xml);

        Path path = Paths.get(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
            writer.write(xml);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String[] getCompany() {
        return this.company;
    }

    public String[] getRenameDirectoryFormat() {
        return this.renameDirectoryFormat;
    }

    public String[] getRenameFileFormat() {
        return this.renameFileFormat;
    }

    public void setCompany(String[] company) {
        this.company = company;
    }

    public void setRenameDirectoryFormat(String[] renameDirectoryFormat) {
        this.renameDirectoryFormat = renameDirectoryFormat;
    }

    public void setRenameFileFormat(String[] renameFileFormat) {
        this.renameFileFormat = renameFileFormat;
    }


}
