/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.dfs;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;

/** 
 * DfsPath is a Path that's been annotated with some extra information.
 * The point of this class is to pass back the "common" metadata about 
 * a file with the names in a directory listing to make accesses faster.
 */
class DfsPath extends Path {
  DFSFileInfo info;

  /**
   * DfsPaths are fully qualified with scheme and authority.
   */
  public DfsPath(DFSFileInfo info, FileSystem fs) {
    super((new Path(info.getPath().toString())).makeQualified(fs).toString());
    this.info = info;
  }

  public boolean isDirectory() {
    return info.isDir();
  }
  public boolean isFile() {
    return ! isDirectory();
  }
  public long length() {
    return info.getLen();
  }
  public long getContentsLength() {
    assert !isDirectory();
    return info.getLen();
  }
  public short getReplication() {
    return info.getReplication();
  }
  public long getBlockSize() {
    return info.getBlockSize();
  }
  public long getModificationTime() {
    return info.getModificationTime();
  }
}
