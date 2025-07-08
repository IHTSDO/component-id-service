package org.snomed.cis.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandlebarsDemoTemplateLoaderTest {

    private HandlebarsDemoTemplateLoader loader;

    @BeforeEach
    void setUp() {
        loader = new HandlebarsDemoTemplateLoader();
        loader.loadHandlebarTemplates();
    }

    @Test
    void testLoadHandlebarTemplates_shouldInitializeHandlebars() {
        assertNotNull(loader.getHandlebars(), "Handlebars should be initialized");
    }

    @Test
    void testGetContext_withJson_shouldBuildContext() throws Exception {
        String json = "{\"name\":\"AasaiTech\",\"course\":\"Internship\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);

        Context context = loader.getContext(jsonNode);

        assertNotNull(context);
        assertEquals("AasaiTech", context.get("name"));
        assertEquals("Internship", context.get("course"));
    }

    @Test
    void testGetTemplate_withMockedHandlebars() throws IOException {
        // Setup mock
        Handlebars mockHandlebars = mock(Handlebars.class);
        Template mockTemplate = mock(Template.class);

        when(mockHandlebars.compile("test-template")).thenReturn(mockTemplate);

        loader.setHandlebars(mockHandlebars); // Inject mocked handlebars

        Template template = loader.getTemplate("test-template");

        assertNotNull(template);
        verify(mockHandlebars, times(1)).compile("test-template");
    }

    @Test
    void testGetTemplate_shouldThrowIOExceptionIfTemplateMissing() {
        assertThrows(IOException.class, () -> loader.getTemplate("non-existent-template"));
    }
}
