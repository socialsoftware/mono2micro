package pt.ist.socialsoftware.mono2micro.element;

public enum ElementType {
    DOMAIN_ENTITY("Domain Entity"),
    METHOD("Method"),
    CLASS("Class");

    private final String text;

    ElementType(final String text) { this.text = text; }

    @Override
    public String toString() { return text; }
}
