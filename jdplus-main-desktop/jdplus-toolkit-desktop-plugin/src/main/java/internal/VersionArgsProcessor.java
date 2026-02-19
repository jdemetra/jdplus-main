package internal;

import nbbrd.io.sys.SystemProperties;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.NbBundle;

import java.util.stream.Stream;

public final class VersionArgsProcessor implements ArgsProcessor {

    @Arg(shortName = 'v', longName = "version", defaultValue = "")
    @Description(shortDescription = "#DESC_VersionArgs")
    @NbBundle.Messages("DESC_VersionArgs=Print version information")
    public String[] versionArgs;

    @Override
    public void process(Env env) throws CommandException {
        Stream.of(getVersion()).forEach(env.getOutputStream()::println);
    }

    private static String[] getVersion() {
        ModuleInfo moduleInfo = Modules.getDefault().ownerOf(VersionArgsProcessor.class);
        SystemProperties sys = SystemProperties.DEFAULT;
        return new String[]{
                "JDemetra+ " + moduleInfo.getSpecificationVersion().toString(),
                "JVM: " + sys.getJavaVersion() + " (" + sys.getJavaVendor() + " " + sys.getJavaVmName() + " " + sys.getJavaVmVersion() + ")",
                "OS: " + sys.getOsName() + " " + sys.getOsVersion() + " " + sys.getOsArch()
        };
    }
}