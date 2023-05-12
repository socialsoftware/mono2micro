package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
public interface DomainEntity {
    void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor);
}