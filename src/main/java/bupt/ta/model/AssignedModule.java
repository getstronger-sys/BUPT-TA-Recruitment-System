package bupt.ta.model;

/**
 * One module that an admin assigns to an MO for the current term.
 */
public class AssignedModule {
    private String moduleCode;
    private String moduleName;

    public AssignedModule() {
    }

    public AssignedModule(String moduleCode, String moduleName) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
