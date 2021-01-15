/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.service.ScanServiceImpl.MAX_FACES_TO_RECOGNIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.PluginsVersions;
import com.exadel.frs.core.trainservice.util.MultipartFileData;
import java.io.IOException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartFile;

@DataJpaTest
@Import({ScanServiceImpl.class, FacesFeignClient.class, FaceDao.class, FaceCacheProvider.class})
class ScanServiceImplTestIT {

    @Autowired
    private ScanServiceImpl scanService;

    @MockBean
    private FacesApiClient facesApiClient;

    private static final MultipartFile MULTIPART_FILE_DATA = new MultipartFileData("hex-string-1".getBytes(), "test", "application/json");
    private static final String FACE_NAME = "faceName";
    private static final String MODEL_KEY = "modelKey";
    private static final double THRESHOLD = 1.0;
    private static final double EMBEDDING = 100500;
    private static final FindFacesResponse SCAN_RESULT = FindFacesResponse.builder()
                                                                          .pluginsVersions(PluginsVersions.builder()
                                                                                                          .calculator("1.0")
                                                                                                          .build())
                                                                          .result(List.of(FindFacesResult.builder()
                                                                                                         .embedding(new Double[]{EMBEDDING})
                                                                                                         .build())
                                                                          )
                                                                          .build();

    @Test
    public void scanAndFaceTest() throws IOException {
        when(facesApiClient.findFacesWithCalculator(MULTIPART_FILE_DATA, MAX_FACES_TO_RECOGNIZE, THRESHOLD, null)).thenReturn(SCAN_RESULT);

        val actual = scanService.scanAndSaveFace(MULTIPART_FILE_DATA, FACE_NAME, THRESHOLD, MODEL_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(FACE_NAME);
    }
}