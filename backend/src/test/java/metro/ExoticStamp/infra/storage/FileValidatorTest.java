package metro.ExoticStamp.infra.storage;

import metro.ExoticStamp.common.exceptions.storage.FileTooLargeException;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileValidatorTest {

    private FileValidator validator;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.getFile().setMaxSizeMb(5);
        props.getFile().setAllowedTypes(List.of("image/jpeg", "image/png"));
        validator = new FileValidator(props);
    }

    @Test
    void validate_validJpeg_passes() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", new byte[10]);
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_invalidType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.gif", "image/gif", new byte[10]);
        assertThrows(InvalidImageTypeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_exceedsSize_throws() {
        byte[] huge = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", huge);
        assertThrows(FileTooLargeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", new byte[0]);
        assertThrows(InvalidFileException.class, () -> validator.validate(file));
    }

    @Test
    void validate_webp_passes() {
        StorageProperties props = new StorageProperties();
        props.getFile().setMaxSizeMb(5);
        props.getFile().setAllowedTypes(List.of("image/jpeg", "image/png", "image/webp"));
        FileValidator webpValidator = new FileValidator(props);
        MockMultipartFile file = new MockMultipartFile("file", "a.webp", "image/webp", new byte[10]);
        assertDoesNotThrow(() -> webpValidator.validate(file));
    }
}
