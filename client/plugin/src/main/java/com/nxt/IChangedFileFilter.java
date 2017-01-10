package com.nxt;

import com.nxt.config.Asset;

/**
 * Created by alex on 10/01/2017.
 */
interface IChangedFileFilter {
  boolean hasLocalModifications(Asset asset);
}
