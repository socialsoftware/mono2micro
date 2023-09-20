package util.property_scanner;

import java.util.ArrayList;
import java.util.List;

import collectors.SpoonCollector;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtLoopImpl;

public class ObjectComparisonPropertyScanner extends PropertyScanner {
    private static List<CtTypeReference<?>> acceptedTypes = null;

    public ObjectComparisonPropertyScanner() {
        if (acceptedTypes == null) {
            acceptedTypes = new ArrayList<CtTypeReference<?>>();
            TypeFactory tf = new TypeFactory();

            acceptedTypes.add(tf.integerPrimitiveType());
            acceptedTypes.add(tf.integerType());
            acceptedTypes.add(tf.bytePrimitiveType());
            acceptedTypes.add(tf.byteType());
            acceptedTypes.add(tf.characterPrimitiveType());
            acceptedTypes.add(tf.characterType());
            acceptedTypes.add(tf.booleanPrimitiveType());
            acceptedTypes.add(tf.booleanType());
            acceptedTypes.add(tf.doublePrimitiveType());
            acceptedTypes.add(tf.doubleType());
            acceptedTypes.add(tf.floatPrimitiveType());
            acceptedTypes.add(tf.floatType());
            acceptedTypes.add(tf.longPrimitiveType());
            acceptedTypes.add(tf.longType());
            acceptedTypes.add(tf.shortPrimitiveType());
            acceptedTypes.add(tf.shortType());
            acceptedTypes.add(tf.voidPrimitiveType());
            acceptedTypes.add(tf.voidType());
            acceptedTypes.add(tf.stringType());
            acceptedTypes.add(tf.nullType());
        }
    }

    public void process(SpoonCollector collector, CtElement element) {
        if (element instanceof CtBinaryOperator && (element.getParent() instanceof CtLoopImpl || element.getParent() instanceof CtIfImpl) && (element.getRoleInParent() == CtRole.EXPRESSION || element.getRoleInParent() == CtRole.CONDITION)) {
            if(evaluateExpression(collector, (CtBinaryOperator<?>)element)) {
                collector.addLabel("object_comparison");
            }
        }
    }

    boolean evaluateExpression(SpoonCollector collector, CtBinaryOperator<?> element) {
        BinaryOperatorKind op = element.getKind();
        
        // right side
        CtExpression<?> rightSide = element.getRightHandOperand();
        boolean rightSideResult = false;
        if (rightSide instanceof CtBinaryOperator) {
            rightSideResult = evaluateExpression(collector, (CtBinaryOperator<?>)rightSide);
        }else if (rightSide instanceof CtVariableAccess) {
            if(!isPrimitiveType(rightSide.getType())) {
                if ((op.equals(BinaryOperatorKind.LT) || op.equals(BinaryOperatorKind.LE) || op.equals(BinaryOperatorKind.EQ))) {
                    return true;
                }
            }
         }

        // left side
        CtExpression<?> leftSide = element.getLeftHandOperand();
        boolean leftSideResult = false;
        if (leftSide instanceof CtBinaryOperator) {
            leftSideResult = evaluateExpression(collector, (CtBinaryOperator<?>)leftSide);
        } else if (leftSide instanceof CtVariableAccess) {
            if(!isPrimitiveType(leftSide.getType())) {
                if ((op.equals(BinaryOperatorKind.LT) || op.equals(BinaryOperatorKind.LE) || op.equals(BinaryOperatorKind.EQ))) {
                    return true;
                }
            }
            
        }

        if (op.equals(BinaryOperatorKind.AND)) {
            return rightSideResult || leftSideResult;
        } else if (op.equals(BinaryOperatorKind.OR)) {
            return rightSideResult && leftSideResult;
        }

        return false;
    }

    boolean isPrimitiveType(CtTypeReference<?> objectType) {
        if (objectType == null) return false;

        CtTypeReference<?> supertype = objectType;
        do {
            if (acceptedTypes.contains(supertype)) return true;

            supertype = supertype.getSuperclass();
        } while(supertype != null);

        return false;
    }

}
