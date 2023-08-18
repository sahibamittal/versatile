package io.github.nscuro.versatile;

import io.github.nscuro.versatile.version.NpmVersion;
import io.github.nscuro.versatile.version.VersioningScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class VersTest {

    @Test
    void testParse() {
        final Vers vers = Vers.parse("vers:npm/1.2.3|>=2.0.0|<5.0.0");
        assertThat(vers).isNotNull();
        assertThat(vers.scheme()).isEqualTo(VersioningScheme.NPM);
        assertThat(vers.constraints()).satisfiesExactly(
                constraint -> {
                    assertThat(constraint).isNotNull();
                    assertThat(constraint.comparator()).isEqualTo(Comparator.EQUAL);
                    assertThat(constraint.version()).isInstanceOf(NpmVersion.class);
                    assertThat(constraint.version()).hasToString("1.2.3");
                },
                constraint -> {
                    assertThat(constraint).isNotNull();
                    assertThat(constraint.comparator()).isEqualTo(Comparator.GREATER_THAN_OR_EQUAL);
                    assertThat(constraint.version()).isInstanceOf(NpmVersion.class);
                    assertThat(constraint.version()).hasToString("2.0.0");
                },
                constraint -> {
                    assertThat(constraint).isNotNull();
                    assertThat(constraint.comparator()).isEqualTo(Comparator.LESS_THAN);
                    assertThat(constraint.version()).isInstanceOf(NpmVersion.class);
                    assertThat(constraint.version()).hasToString("5.0.0");
                }
        );
    }

    @Test
    void testBuild() {
        final Vers vers = Vers.builder(VersioningScheme.MAVEN)
                .withConstraint(Comparator.LESS_THAN, "6.6.6")
                .withConstraint(Comparator.GREATER_THAN_OR_EQUAL, "1.2.3")
                .withConstraint(Comparator.NOT_EQUAL, "3.2.1")
                .withConstraint("< 0.5.1")
                .build();

        assertThat(vers).hasToString("vers:maven/<0.5.1|>=1.2.3|!=3.2.1|<6.6.6");
        assertThat(vers.contains("1.2.2")).isFalse();
        assertThat(vers.contains("2.3.4")).isTrue();
        assertThat(vers.contains("3.2.1")).isFalse();
        assertThat(vers.contains("3.2.2")).isTrue();
        assertThat(vers.contains("6.6.5")).isTrue();
        assertThat(vers.contains("6.6.6")).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "vers:pypi/>0.0.0|>=0.0.1|0.0.2|<0.0.3|0.0.4|<0.0.5|>=0.0.6,vers:pypi/>0.0.0|<0.0.5|>=0.0.6",
            "vers:pypi/>0.0.0|>=0.0.1|0.0.2|0.0.3|0.0.4|<0.0.5|>=0.0.6|!=0.8,vers:pypi/>0.0.0|<0.0.5|>=0.0.6|!=0.8",
            "vers:pypi/>0.0.0|>=0.0.1|>=0.0.1|0.0.2|0.0.3|0.0.4|<0.0.5|<=0.0.6|!=0.7|8.0|>12|<15.3,vers:pypi/>0.0.0|<=0.0.6|!=0.7|8.0|>12|<15.3"
    })
    void testSimplify(final String before, final String after) {
        final Vers vers = Vers.parse(before).simplify();
        assertThat(vers).hasToString(after);
        assertThatNoException().isThrownBy(vers::validate);
    }

}