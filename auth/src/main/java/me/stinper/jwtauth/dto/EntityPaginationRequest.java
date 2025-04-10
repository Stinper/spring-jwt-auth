package me.stinper.jwtauth.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record EntityPaginationRequest(

        @Parameter(
                in = ParameterIn.QUERY,
                description = "Номер страницы с записями",
                example = "0",
                required = true,
                schema = @Schema(minLength = 0, type = "int")
        )
        @NotNull(message = "{messages.entity-pagination-request.page.null}")
        @Min(value = 0, message = "{messages.entity-pagination-request.validation.page.negative}")
        Integer page,

        @Parameter(
                in = ParameterIn.QUERY,
                description = "Количество записей, которое будет отображаться на одной странице",
                example = "10",
                required = true,
                schema = @Schema(minLength = 0, maxLength = 100, type = "int")
        )
        @NotNull(message = "{messages.entity-pagination-request.size.null}")
        @Min(value = 0, message = "{messages.entity-pagination-request.validation.size.negative}")
        @Max(value = 100, message = "{messages.entity-pagination-request.validation.size.too-big}")
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
