package org.egov.filestore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class FileLocation {
    private String fileStoreId;
    private String module;
    private String tag;
    private String tenantId;
    private String fileName;
}
