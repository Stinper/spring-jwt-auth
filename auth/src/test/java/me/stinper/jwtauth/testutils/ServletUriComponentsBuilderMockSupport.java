package me.stinper.jwtauth.testutils;

import org.mockito.MockedStatic;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static org.mockito.Mockito.*;

public final class ServletUriComponentsBuilderMockSupport {
    private ServletUriComponentsBuilderMockSupport() {}

    /**
     * Мокает логику построения URI из класса {@link ServletUriComponentsBuilder}. Может использоваться, к примеру,
     * в Unit-тестах @PostMapping методов контроллера, в которых при создании ресурса нужно вернуть заголовок <br>
     * <code>Location: [URI]</code>
     * @param expectedURi URI, который должен быть возвращен классом ServletUriComponentsBuilder
     * @param testLogic логика тестового метода, которая должна быть выполнена с Mock-классом ServletUriComponentsBuilder
     */
    public static void withMockedUriComponentsBuilder(URI expectedURi, Runnable testLogic) {
        try(MockedStatic<ServletUriComponentsBuilder> servletUriComponentsBuilderMock = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builderMock = mock(ServletUriComponentsBuilder.class);
            UriComponentsBuilder uriBuilderMock = mock(UriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            servletUriComponentsBuilderMock.when(ServletUriComponentsBuilder::fromCurrentRequest).thenReturn(builderMock);

            when(builderMock.path(anyString())).thenReturn(uriBuilderMock);
            when(uriBuilderMock.buildAndExpand(Optional.ofNullable(any()))).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(expectedURi);

            testLogic.run();
        }
    }
}
