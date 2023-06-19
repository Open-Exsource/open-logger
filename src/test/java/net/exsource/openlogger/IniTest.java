package net.exsource.openlogger;

import net.exsource.openlogger.template.FileStorage;
import net.exsource.openlogger.util.ConsoleColor;
import net.exsource.openlogger.util.DevTools;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IniTest {

    private static final Logger logger = Logger.getLogger();

    @Test
    void checkIniCanRead() {
        String testIni = "@[graphics]\n resolution = \"1920x1080\" \n[audio]\n sfx = 100 \ngeneral = 60";

        List<FileStorage> testList = new ArrayList<>();
        DevTools.readIni(testIni, testList);
        for(FileStorage storage : testList) {
            logger.empty("");
            logger.info("===================== " + storage.name() + " =====================");
            logger.info("=>");
            for(String key : storage.objects().keySet()) {
                logger.info("=> " + ConsoleColor.GREEN + key + ConsoleColor.RESET + " - " + storage.objects().get(key));
            }
            logger.info("=>");
            logger.info("===================== " + storage.name() + " =====================");
        }
    }

    @Test
    void checkIniCanWrite() {
        Map<String, Object> generalContent = new HashMap<>();
        generalContent.put("name", "Daniel");
        generalContent.put("lastname", "Ramke");
        FileStorage general = new FileStorage("general", generalContent);

        Map<String, Object>  graphicsContent = new HashMap<>();
        graphicsContent.put("resolution", "1920x1080");
        graphicsContent.put("refreshRate", 60);
        FileStorage graphics = new FileStorage("graphics", graphicsContent);
        DevTools.writeIni("test.ini", general, graphics);
    }

}
