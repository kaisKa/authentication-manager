//package ava.io.authentication_manager.db.specifications;
//
//import ava.io.authentication_manager.db.entities.UserLoginData;
//import ava.io.authentication_manager.model.SearchCriteria;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.data.jpa.domain.Specification;
//
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//
//@Getter
//@Setter
//@AllArgsConstructor
//public class UserSpecification implements Specification<UserLoginData> {
//   private SearchCriteria criteria;
//
//    @Override
//    public Predicate toPredicate(Root<UserLoginData> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
//        if (criteria.getOperation().equalsIgnoreCase(">")) {
//            return builder.greaterThanOrEqualTo(
//                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
//        }
//        else if (criteria.getOperation().equalsIgnoreCase("<")) {
//            return builder.lessThanOrEqualTo(
//                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
//        }
//        else if (criteria.getOperation().equalsIgnoreCase(":")) {
//            if (root.get(criteria.getKey()).getJavaType() == String.class) {
//                return builder.like(
//                        root.<String>get(criteria.getKey()), "%" + criteria.getValue() + "%");
//            } else {
//                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
//            }
//        }
//        return null;
//    }
//
//
//}
