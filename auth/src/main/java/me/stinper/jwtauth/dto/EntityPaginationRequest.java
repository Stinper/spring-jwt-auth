package me.stinper.jwtauth.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record EntityPaginationRequest(

        @Parameter(
                in = ParameterIn.QUERY,
                name = "page",
                description = "Номер страницы с записями. Номера начинаются с 0",
                example = "0",
                required = true
        )
        @NotNull(message = "{messages.entity-pagination-request.page.null}")
        Integer page,

        @Parameter(
                in = ParameterIn.QUERY,
                name = "size",
                description = "Количество записей, которое будет отображаться на одной странице",
                example = "10",
                required = true
        )
        @NotNull(message = "{messages.entity-pagination-request.size.null}")
        Integer size,

        @Parameter(
                in = ParameterIn.QUERY,
                name = "sortBy",
                description = """
                        Имя поля, по которому необходимо провести сортировку полученных записей.
                        Имя поля указывается в формате camelCase
                        """,
                example = "id"
        )
        String sortBy,

        @Parameter(
                in = ParameterIn.QUERY,
                name = "direction",
                description = "Направление сортировки (ASC / DESC)",
                example = "ASC"
        )
        Sort.Direction direction

) {
        public Pageable buildPageableFromRequest() {
                if (this.sortBy == null || this.direction == null)
                        return PageRequest.of(this.page, this.size);

                return PageRequest.of(this.page, this.size, this.direction, sortBy);
        }
}
