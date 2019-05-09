package eu.stamp.botsing;

/*-
 * #%L
 * botsing-reproduction
 * %%
 * Copyright (C) 2017 - 2018 eu.stamp-project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CommandLineParameters {

    public static final String D_OPT = "D";
    public static final String PROJECT_CP_OPT = "project_cp";
    public static final String TARGET_FRAME_OPT = "target_frame";
    public static final String CRASH_LOG_OPT = "crash_log";
    public static final String ENABLE_FLA_OPT = "enable_fla";
    public static final String MODEL_PATH_OPT = "model";
    public static final String PROJECT_PACKAGE = "projectPackage";
    public static final String HELP_OPT = "help";

    public static Options getCommandLineOptions() {
        Options options = new Options();
        // Properties
        options.addOption(Option.builder(D_OPT)
                .numberOfArgs(2)
                .argName("property=value")
                .valueSeparator()
                .desc("use value for given property")
                .build());
        // Classpath
        options.addOption(Option.builder(PROJECT_CP_OPT)
                .hasArg()
                .desc("classpath of the project under test and all its dependencies")
                .build());
        // Target frame
        options.addOption(Option.builder(TARGET_FRAME_OPT)
                .hasArg()
                .desc("Level of the target frame")
                .build());
        // Stack trace file
        options.addOption(Option.builder(CRASH_LOG_OPT)
                .hasArg()
                .desc("File with the stack trace")
                .build());
        // Fitness landscape analysis
        options.addOption(Option.builder(ENABLE_FLA_OPT)
                .desc("Perform fitness landscape analysis")
                .build());
        // Models directory
        options.addOption(Option.builder(MODEL_PATH_OPT)
                .hasArg()
                .desc("Directory of models generated by Botsing model generator")
                .build());

        // Help message
        options.addOption(Option.builder(HELP_OPT)
                .desc("Prints this help message.")
                .build());

        return options;
    }

}
