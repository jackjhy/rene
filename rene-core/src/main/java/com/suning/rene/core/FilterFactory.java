package com.suning.rene.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by tiger on 14-3-29.
 */
public class FilterFactory {
	public static final IFilter AlwaysPresent = new AlwaysPresentFilter();

	private static final Logger logger = LoggerFactory
			.getLogger(FilterFactory.class);
	private static final long BITSET_EXCESS = 20;

	public static final double DEFAULT_FALSE_POS_PROBABILITY = 0.0001;

	public static void serialize(IFilter bf, DataOutput output)
			throws IOException {
		Murmur3BloomFilter.serializer
				.serialize((Murmur3BloomFilter) bf, output);
	}

	public static IFilter deserialize(DataInput input, boolean offheap)
			throws IOException {
		return Murmur3BloomFilter.serializer.deserialize(input, offheap);
	}

	/**
	 * @return A BloomFilter with the lowest practical false positive
	 *         probability for the given number of elements.
	 */
	public static IFilter getFilter(long numElements, int targetBucketsPerElem,
			boolean offheap) {
		int maxBucketsPerElement = Math.max(1,
				BloomCalculations.maxBucketsPerElement(numElements));
		int bucketsPerElement = Math.min(targetBucketsPerElem,
				maxBucketsPerElement);
		if (bucketsPerElement < targetBucketsPerElem) {
			logger.warn(String
					.format("Cannot provide an optimal BloomFilter for %d elements (%d/%d buckets per element).",
							numElements, bucketsPerElement,
							targetBucketsPerElem));
		}
		BloomCalculations.BloomSpecification spec = BloomCalculations
				.computeBloomSpec(bucketsPerElement);
		return createFilter(spec.K, numElements, spec.bucketsPerElement,
				offheap);
	}

	/**
	 * 
	 * @param numElements
	 * @param offheap
	 * @return
	 */
	public static IFilter getFilter(long numElements, boolean offheap) {
		return getFilter(numElements, DEFAULT_FALSE_POS_PROBABILITY, offheap);
	}

	/**
	 * @return The smallest BloomFilter that can provide the given false
	 *         positive probability rate for the given number of elements.
	 * 
	 *         Asserts that the given probability can be satisfied using this
	 *         filter.
	 */
	public static IFilter getFilter(long numElements,
			double maxFalsePosProbability, boolean offheap) {
		assert maxFalsePosProbability <= 1.0 : "Invalid probability";
		if (maxFalsePosProbability == 1.0)
			return new AlwaysPresentFilter();
		int bucketsPerElement = BloomCalculations
				.maxBucketsPerElement(numElements);
		BloomCalculations.BloomSpecification spec = BloomCalculations
				.computeBloomSpec(bucketsPerElement, maxFalsePosProbability);
		return createFilter(spec.K, numElements, spec.bucketsPerElement,
				offheap);
	}

	private static IFilter createFilter(int hash, long numElements,
			int bucketsPer, boolean offheap) {
		long numBits = (numElements * bucketsPer) + BITSET_EXCESS;
		IBitSet bitset = offheap ? new OffHeapBitSet(numBits) : new OpenBitSet(
				numBits);
		return new Murmur3BloomFilter(hash, bitset);
	}
}
