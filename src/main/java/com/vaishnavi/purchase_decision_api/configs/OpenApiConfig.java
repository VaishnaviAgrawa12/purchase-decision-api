package com.vaishnavi.purchase_decision_api.configs;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    private static final String DESCRIPTION = """
            Answers **"Should I buy this?"** with a verdict (BUY / WAIT / SKIP), an
            affordability score, and — when the answer is WAIT — a savings plan.

            ### Just want to see it work? Two clicks.

            1. **`POST /api/auth/demo`** — **Try it out** → **Execute**. No body needed.
               You get a token for a throwaway account that **already has a financial
               profile**. Copy the `token`.
            2. Click the green **Authorize** button at the top right, paste the token,
               Authorize, Close.
            3. **`POST /api/decision`** — **Try it out** → **Execute**. Pick any of the
               example purchases; each one lands on a different verdict.

            Paste *just the token* — no `Bearer ` prefix, Swagger adds that itself. It is
            remembered across page reloads.

            ### Using your own account instead

            **`POST /api/auth/register`** (change the example email — it has to be unique),
            or **`POST /api/auth/login`** with the shared demo login below. Then
            **`PUT /api/users/profile`** to set your income and expenses before calling
            `POST /api/decision` — without a profile there's nothing to score against and
            the decision endpoint returns `400`.

            | | |
            |---|---|
            | Shared demo login | `demo@purchasedecision.app` / `Demo@1234` |
            | Its disposable income | ₹24,500/month (₹62,500 income − ₹25,500 fixed − 20% saved) |

            The shared account is reset every time the service restarts, so anyone can
            edit its profile without breaking it for long. `POST /api/auth/demo` avoids
            the shared state entirely.

            Every endpoint ships with a pre-filled example body: hit **Try it out**,
            then **Execute**, and edit the values from there.
            """;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Purchase Decision API")
                        .version("1.0")
                        .description(DESCRIPTION)
                        .contact(new Contact().name("Vaishnavi Agrawal"))
                        .license(new License().name("MIT")))
                // Applied to every operation by default; the public auth endpoints opt
                // out with an empty @SecurityRequirements.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Paste the raw JWT returned by /api/auth/register "
                                        + "or /api/auth/login. Do not include the \"Bearer \" prefix.")));
    }
}
