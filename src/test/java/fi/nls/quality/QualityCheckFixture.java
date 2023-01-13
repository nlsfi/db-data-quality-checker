/*  
  Copyright (C) 2022 National Land Survey of Finland
  (https://www.maanmittauslaitos.fi/en).


  This file is part of quality-service.

  quality-service is free software: you can redistribute it and/or
  modify it under the terms of the GNU General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  quality-service is distributed in the hope that it will be
  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with quality-service. If not, see <https://www.gnu.org/licenses/>.
*/
package fi.nls.quality;

import java.util.*;
import fi.nls.quality.model.Description;

public class QualityCheckFixture {
    public static List<Description> createDescriptions() {
        List<Description> descriptions = new ArrayList<>();
        Arrays.asList("fi", "sv", "en").forEach(lang -> {
            Description description = new Description();
            description.setLang(lang);
            description.setDescription(lang + "_sääntö");
            descriptions.add(description);
        });
        return descriptions;
    }
}
