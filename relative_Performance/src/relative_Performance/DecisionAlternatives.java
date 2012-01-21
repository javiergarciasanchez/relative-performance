package relative_Performance;

import java.util.Iterator;
import java.util.NoSuchElementException;

/*
 * It does not return the current position as an alternative
 */

public class DecisionAlternatives implements Iterable<int[]> {
	Firm firm;

	public DecisionAlternatives(Firm firm) {
		this.firm = firm;
	}

	public Iterator<int[]> iterator() {
		return new DecAltIterator();

	}

	public class DecAltIterator implements Iterator<int[]> {
		int dim = 0, sign = 1;
		int[] nextDec = null;

		@Override
		public boolean hasNext() {
			Firm firm = DecisionAlternatives.this.firm;
			int dimensions = firm.dSpace.getDimensions().size();
			int[] currPos = firm.dSpace.getLocation(firm).toIntArray(null);
			
			if (dim >= dimensions) return false;
			int size = firm.dSpace.getDimensions().getDimension(dim);			

			while (dim < dimensions) {
				if ((sign == 1) && (currPos[dim] < size - 1)) {
					return true;
				}

				else if ((sign == 1) && (currPos[dim] == size - 1)) {
					sign = -1;
					continue;
				}

				if ((sign == -1) && (currPos[dim] > 0)) {
					return true;
				}

				else if ((sign == -1) && (currPos[dim] == 0)) {
					sign = 1;
					dim++;
					continue;
				}

			}

			return false;

		}

		@Override
		public int[] next() {

			if (!hasNext()) {
				throw new NoSuchElementException(
						"Trying to access a non existent alternative");
			} else {
				int[] dec = firm.dSpace.getLocation(firm).toIntArray(null);
				dec[dim] = dec[dim] + sign;

				if (sign == -1)
					dim++;
				sign *= -1;

				return dec;
			}

		}

		@Override
		public void remove() {
		}

	}
}