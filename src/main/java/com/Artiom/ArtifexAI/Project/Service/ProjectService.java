package com.Artiom.ArtifexAI.Project.Service;

import com.Artiom.ArtifexAI.Project.DTO.ProjectCreateDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectEditDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectInstructionUpdateDTO;

import java.util.List;

public interface ProjectService {

    ProjectDTO createProject(ProjectCreateDTO projectCreateDTO);

    void deleteProject(Long projectId);

    void editProject (ProjectEditDTO projectEditDTO);

    void updateInstructionList(ProjectInstructionUpdateDTO projectInstructionUpdateDTO);

    void addInstructionString(ProjectInstructionUpdateDTO projectInstructionUpdateDTO);

    ProjectDTO getProject(Long projectId);

    List<ProjectDTO> getALlProjects();
}
