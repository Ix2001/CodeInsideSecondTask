import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipArchiver {

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("help")) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "create":
                if (args.length < 3) {
                    System.out.println("Необходимо указать как минимум два аргумента.");
                    return;
                }
                createZip(args);
                break;
            case "extract":
                if (args.length < 3) {
                    System.out.println("Необходимо указать как минимум два аргумента.");
                    return;
                }
                extractZip(args);
                break;
            default:
                System.out.println("Неверная команда: " + args[0]);
        }
    }

    private static void createZip(String[] args) {
        String zipFileName = args[1];
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            for (int i = 2; i < args.length; i++) {
                Path filePath = Path.of(args[i]);
                if (Files.exists(filePath)) {
                    if (Files.isRegularFile(filePath)) {
                        addFileToZip(zipOutputStream, filePath);
                    } else {
                        System.out.println("Путь " + args[i] + " указывает на директорию, а не на файл и будет пропущен.");
                    }
                } else {
                    System.out.println("Файл " + args[i] + " не найден и будет пропущен.");
                }
            }
            System.out.println("Архив " + zipFileName + " успешно создан.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addFileToZip(ZipOutputStream zipOutputStream, Path filePath) throws IOException {
        ZipEntry entry = new ZipEntry(filePath.getFileName().toString());
        zipOutputStream.putNextEntry(entry);
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, length);
            }
        }
        zipOutputStream.closeEntry();
    }

    private static void extractZip(String[] args) {
        String zipFileName = args[1];
        String outputDirectory = args[2];
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFileName))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                Path outputPath = Path.of(outputDirectory, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                } else {
                    Files.createDirectories(outputPath.getParent());
                    try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
            System.out.println("Архив " + zipFileName + " успешно разархивирован в " + outputDirectory + ".");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println(
                "Простой ZIP-архиватор на Java\n\n" +
                        "Использование:\n" +
                        "create <имя_архива.zip> <путь_к_файлу1> [<путь_к_файлу2>] ... - создать архив из указанных файлов\n" +
                        "extract <путь_к_архиву.zip> <выходная_папка> - разархивировать архив в указанную папку\n" +
                        "help - показать это сообщение"
        );
    }
}
