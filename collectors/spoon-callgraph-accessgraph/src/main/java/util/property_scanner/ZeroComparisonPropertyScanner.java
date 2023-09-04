package collectors.util.property_scanner;

import collectors.SpoonCollector;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtLoopImpl;

public class ZeroComparisonPropertyScanner extends PropertyScanner {
    public void process(SpoonCollector collector, CtElement element) {
        if (element instanceof CtBinaryOperator && (element.getParent() instanceof CtLoopImpl || element.getParent() instanceof CtIfImpl) && (element.getRoleInParent() == CtRole.EXPRESSION || element.getRoleInParent() == CtRole.CONDITION)) {
            if(evaluateExpression(collector, (CtBinaryOperator)element)) {
                collector.addLabel("zero_comparison");
            }
        }
    }
    
    boolean evaluateExpression(SpoonCollector collector, CtBinaryOperator element) {
        BinaryOperatorKind op = element.getKind();
        
        // right side
        CtExpression rightSide = element.getRightHandOperand();
        boolean rightSideResult = false;
        if (rightSide instanceof CtLiteral) {
            if(Integer.class.isInstance(((CtLiteral)rightSide).getValue()) && (Integer)((CtLiteral)rightSide).getValue() == 0) {
                if ((op.equals(BinaryOperatorKind.LT) || op.equals(BinaryOperatorKind.LE) || op.equals(BinaryOperatorKind.EQ))) {
                    return true;
                }
            }
            
        } else if (rightSide instanceof CtBinaryOperator) {
            rightSideResult = evaluateExpression(collector, (CtBinaryOperator)rightSide);
        }

        // left side
        CtExpression leftSide = element.getLeftHandOperand();
        boolean leftSideResult = false;
        if (leftSide instanceof CtLiteral) {
            if(Integer.class.isInstance(((CtLiteral)leftSide).getValue()) && (Integer)((CtLiteral)leftSide).getValue() == 0) {
                if ((op.equals(BinaryOperatorKind.LT) || op.equals(BinaryOperatorKind.LE) || op.equals(BinaryOperatorKind.EQ))) {
                    return true;
                }
            }
            
        } else if (leftSide instanceof CtBinaryOperator) {
            leftSideResult = evaluateExpression(collector, (CtBinaryOperator)leftSide);
        }

        if (op.equals(BinaryOperatorKind.AND)) {
            return rightSideResult || leftSideResult;
        } else if (op.equals(BinaryOperatorKind.OR)) {
            return rightSideResult && leftSideResult;
        }

        return false;
    }
}
