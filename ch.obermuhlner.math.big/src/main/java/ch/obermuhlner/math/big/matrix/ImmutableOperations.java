package ch.obermuhlner.math.big.matrix;

import ch.obermuhlner.math.big.BigDecimalMath;
import ch.obermuhlner.math.big.matrix.internal.AbstractBigMatrix;
import ch.obermuhlner.math.big.matrix.internal.MatrixUtils;
import ch.obermuhlner.math.big.matrix.internal.dense.DenseImmutableBigMatrix;
import ch.obermuhlner.math.big.matrix.internal.sparse.SparseImmutableBigMatrix;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.math.BigDecimal.*;

public class ImmutableOperations {

    public static ImmutableBigMatrix denseAdd(BigMatrix left, BigMatrix right, MathContext mathContext) {
        MatrixUtils.checkSameSize(left, right);

        return ImmutableBigMatrix.matrix(left.lazyAdd(right, mathContext));
    }

    public static ImmutableBigMatrix denseSubtract(BigMatrix left, BigMatrix right, MathContext mathContext) {
        MatrixUtils.checkSameSize(left, right);

        return ImmutableBigMatrix.matrix(left.lazySubtract(right, mathContext));
    }

    public static ImmutableBigMatrix denseMultiply(BigMatrix left, BigDecimal right, MathContext mathContext) {
        return ImmutableBigMatrix.matrix(left.lazyMultiply(right, mathContext));
    }

    public static ImmutableBigMatrix denseMultiply(BigMatrix left, BigMatrix right, MathContext mathContext) {
        MatrixUtils.checkColumnsOtherRows(left, right);

        int rows = left.rows();
        int columns = right.columns();

        AbstractBigMatrix result;
        if (MatrixUtils.isSparseWithLotsOfZeroes(left) && MatrixUtils.isSparseWithLotsOfZeroes(right)) {
            result = new SparseImmutableBigMatrix(rows, columns);
        } else {
            result = new DenseImmutableBigMatrix(rows, columns);
        }

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int index = 0; index < left.columns(); index++) {
                    BigDecimal value = MatrixUtils.multiply(left.get(row, index), right.get(index, column), mathContext);
                    sum = MatrixUtils.add(sum, value, mathContext);
                }
                result.internalSet(row, column, sum.stripTrailingZeros());
            }
        }

        return result.asImmutableMatrix();
    }

    public static ImmutableBigMatrix denseElementOperation(BigMatrix matrix, Function<BigDecimal, BigDecimal> operation) {
        return ImmutableBigMatrix.matrix(matrix.lazyElementOperation(operation));
    }

    public static ImmutableBigMatrix denseTranspose(BigMatrix matrix) {
        return ImmutableBigMatrix.matrix(matrix.lazyTranspose());
    }

    public static BigDecimal denseSum(BigMatrix matrix, MathContext mathContext) {
        BigDecimal result = ZERO;
        for (int row = 0; row < matrix.rows(); row++) {
            for (int col = 0; col < matrix.columns(); col++) {
                result = MatrixUtils.add(result, matrix.get(row, col), mathContext);
            }
        }
        return result;
    }

    public static BigDecimal denseProduct(BigMatrix matrix, MathContext mathContext) {
        BigDecimal result = ONE;
        for (int row = 0; row < matrix.rows(); row++) {
            for (int col = 0; col < matrix.columns(); col++) {
                result = MatrixUtils.multiply(result, matrix.get(row, col), mathContext);
            }
        }
        return result;
    }

    public static ImmutableBigMatrix denseRound(BigMatrix matrix, MathContext mathContext) {
        return ImmutableBigMatrix.matrix(matrix.lazyRound(mathContext));
    }

    public static boolean denseEquals(BigMatrix left, BigMatrix right) {
        if (left == right) return true;

        if (left.rows() != right.rows()) {
            return false;
        }
        if (left.columns() != right.columns()) {
            return false;
        }
        for (int row = 0; row < left.rows(); row++) {
            for (int column = 0; column < left.columns(); column++) {
                if (left.get(row, column).compareTo(right.get(row, column)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean sparseEquals(BigMatrix left, BigMatrix right) {
        if (left == right) return true;

        if (left.rows() != right.rows()) {
            return false;
        }
        if (left.columns() != right.columns()) {
            return false;
        }

        if (left.sparseEmptySize() != 0 && right.sparseEmptySize() != 0 && left.getSparseDefaultValue().compareTo(right.getSparseDefaultValue()) != 0) {
            return false;
        }

        Set<Coord> mergedCoords = left.getCoords().collect(Collectors.toSet());
        mergedCoords.addAll(right.getCoords().collect(Collectors.toSet()));

        for (Coord coord : mergedCoords) {
            if (left.get(coord.row, coord.column).compareTo(right.get(coord.row, coord.column)) != 0) {
                return false;
            }
        }

        return true;
    }

    public static ImmutableBigMatrix sparseAdd(BigMatrix left, BigMatrix right, MathContext mathContext) {
        MatrixUtils.checkSameSize(left, right);

        BigDecimal defaultValue = MatrixUtils.add(left.getSparseDefaultValue(), right.getSparseDefaultValue(), mathContext);

        SparseImmutableBigMatrix m = new SparseImmutableBigMatrix(defaultValue, left.rows(), left.columns());

        Set<Coord> mergedCoords = left.getCoords().collect(Collectors.toSet());
        mergedCoords.addAll(right.getCoords().collect(Collectors.toSet()));

        for (Coord coord : mergedCoords) {
            BigDecimal value = MatrixUtils.add(left.get(coord.row, coord.column), right.get(coord.row, coord.column), mathContext);
            m.internalSet(coord.row, coord.column, value);
        }

        return m;
    }

    public static ImmutableBigMatrix sparseSubtract(BigMatrix left, BigMatrix right, MathContext mathContext) {
        MatrixUtils.checkSameSize(left, right);

        BigDecimal defaultValue = MatrixUtils.subtract(left.getSparseDefaultValue(), right.getSparseDefaultValue(), mathContext);

        SparseImmutableBigMatrix m = new SparseImmutableBigMatrix(defaultValue, left.rows(), left.columns());

        Set<Coord> mergedCoords = left.getCoords().collect(Collectors.toSet());
        mergedCoords.addAll(right.getCoords().collect(Collectors.toSet()));

        for (Coord coord : mergedCoords) {
            BigDecimal value = MatrixUtils.subtract(left.get(coord.row, coord.column), right.get(coord.row, coord.column), mathContext);
            m.internalSet(coord.row, coord.column, value);
        }

        return m;
    }

    public static ImmutableBigMatrix sparseMultiply(BigMatrix left, BigDecimal right, MathContext mathContext) {
        BigDecimal defaultValue = MatrixUtils.multiply(left.getSparseDefaultValue(), right, mathContext);

        SparseImmutableBigMatrix m = new SparseImmutableBigMatrix(defaultValue, left.rows(), left.columns());

        left.getCoordValues().forEach(coordValue -> {
            BigDecimal value = MatrixUtils.multiply(left.get(coordValue.coord.row, coordValue.coord.column), right, mathContext);
            m.internalSet(coordValue.coord.row, coordValue.coord.column, value);
        });

        return m;
    }

    public static ImmutableBigMatrix sparseMultiply(BigMatrix left, BigMatrix right, MathContext mathContext) {
        MatrixUtils.checkColumnsOtherRows(left, right);

        Map<Integer, Map<Integer, BigDecimal>> leftByRowColumn = left.toSparseNestedMap();
        Map<Integer, Map<Integer, BigDecimal>> rightByColumnRow = right.toTransposedSparseNestedMap();

        int rows = left.rows();
        int columns = right.columns();

        SparseImmutableBigMatrix result = new SparseImmutableBigMatrix(rows, columns);

        for (Map.Entry<Integer, Map<Integer, BigDecimal>> leftRow : leftByRowColumn.entrySet()) {
            for (Map.Entry<Integer, Map<Integer, BigDecimal>> rightColumn : rightByColumnRow.entrySet()) {
                BigDecimal sum = BigDecimal.ZERO;
                Set<Integer> commonIndices = new HashSet<>(leftRow.getValue().keySet());
                commonIndices.retainAll(rightColumn.getValue().keySet());
                for (Integer index : commonIndices) {
                    BigDecimal v = MatrixUtils.multiply(leftRow.getValue().get(index), rightColumn.getValue().get(index), mathContext);
                    sum = MatrixUtils.add(sum, v, mathContext);
                }
                result.internalSet(leftRow.getKey(), rightColumn.getKey(), sum);
            }
        }

        return result;
    }

    public static ImmutableBigMatrix sparseElementOperation(BigMatrix matrix, Function<BigDecimal, BigDecimal> operation) {
        BigDecimal defaultValue = operation.apply(matrix.getSparseDefaultValue());

        SparseImmutableBigMatrix result = new SparseImmutableBigMatrix(defaultValue, matrix.rows(), matrix.columns());

        matrix.getCoordValues().forEach(cv -> {
            result.internalSet(cv.coord.row, cv.coord.column, operation.apply(cv.value));
        });

        return result;
    }

    public static ImmutableBigMatrix sparseTranspose(BigMatrix matrix) {
        BigDecimal defaultValue = matrix.getSparseDefaultValue();

        SparseImmutableBigMatrix result = new SparseImmutableBigMatrix(defaultValue, matrix.columns(), matrix.rows());

        matrix.getCoordValues().forEach(cv -> {
            result.internalSet(cv.coord.column, cv.coord.row, cv.value);
        });

        return result;
    }

    public static BigDecimal sparseSum(BigMatrix matrix, MathContext mathContext) {
        BigDecimal common = MatrixUtils.multiply(valueOf(matrix.sparseEmptySize()), matrix.getSparseDefaultValue(), mathContext);

        BigDecimal valuesSum = matrix.getCoordValues()
                .map(cv -> cv.value)
                .reduce(ZERO, (b1, b2) -> MatrixUtils.add(b1, b2, mathContext));

        return MatrixUtils.add(common, valuesSum, mathContext);
    }

    public static BigDecimal sparseProduct(BigMatrix matrix, MathContext mathContext) {
        BigDecimal common;
        if (mathContext == null) {
            common = MatrixUtils.pow(matrix.getSparseDefaultValue(), matrix.sparseEmptySize());
        } else {
            common = BigDecimalMath.pow(matrix.getSparseDefaultValue(), matrix.sparseEmptySize(), mathContext);
        }

        if (common.signum() == 0) {
            return common;
        }

        BigDecimal valuesProduct = matrix.getCoordValues()
                .map(cv -> cv.value)
                .reduce(ONE, (b1, b2) -> MatrixUtils.multiply(b1, b2, mathContext));

        return MatrixUtils.multiply(common, valuesProduct, mathContext);
    }

    public static ImmutableBigMatrix sparseRound(BigMatrix matrix, MathContext mathContext) {
        BigDecimal defaultValue = matrix.getSparseDefaultValue().round(mathContext).stripTrailingZeros();

        SparseImmutableBigMatrix result = new SparseImmutableBigMatrix(defaultValue, matrix.columns(), matrix.rows());

        matrix.getCoordValues().forEach(cv -> {
            result.internalSet(cv.coord.row, cv.coord.column, cv.value.round(mathContext).stripTrailingZeros());
        });
        return result;
    }
}