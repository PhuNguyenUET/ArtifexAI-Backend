package com.Artiom.ArtifexAI.Project.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Image.Service.AlbumService;
import com.Artiom.ArtifexAI.Project.DTO.ProjectCreateDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectEditDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectInstructionUpdateDTO;
import com.Artiom.ArtifexAI.Project.Model.Project;
import com.Artiom.ArtifexAI.Project.Repository.ProjectRepository;
import com.Artiom.ArtifexAI.Project.Service.ProjectService;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization.PromptOptimizationService;
import com.Artiom.ArtifexAI.User.Model.User;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final AlbumService albumService;
    private final PromptOptimizationService promptOptimizationService;
    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Project.class, ProjectDTO.class).setConverter(context -> {
            Project project = context.getSource();

            return ProjectDTO.builder()
                    .id(project.getId())
                    .projectName(project.getProjectName())
                    .instructions(project.getInstructions())
                    .artStyle(project.getArtStyle())
                    .createdDate(project.getCreatedDate())
                    .modifiedDate(project.getModifiedDate())
                    .build();
        });
    }

    @Override
    public ProjectDTO createProject(ProjectCreateDTO projectCreateDTO) {
        List<String> optimizedInstructions = promptOptimizationService.optimizeInstruction(projectCreateDTO.getInstructions());

        Project project = Project.builder()
                .projectName(projectCreateDTO.getProjectName())
                .instructions(optimizedInstructions)
                .artStyle(projectCreateDTO.getArtStyle())
                .userId(AuthenticationUtils.getCurrentUser().getId())
                .build();

        Project savedProject = projectRepository.save(project);
        albumService.createAlbumForProject("Project: " + savedProject.getProjectName(), savedProject.getId());

        return modelMapper.map(savedProject, ProjectDTO.class);
    }

    @Override
    public void deleteProject(String projectId) {
        Project project = getAndCheckProject(projectId);

        albumService.unlinkProjectAlbum(projectId);
        projectRepository.delete(project);
    }

    @Override
    public void editProject(ProjectEditDTO projectEditDTO) {
        Project project = getAndCheckProject(projectEditDTO.getProjectId());

        if(projectEditDTO.getProjectName() != null && !projectEditDTO.getProjectName().isEmpty()) {
            project.setProjectName(projectEditDTO.getProjectName());
        }

        if(projectEditDTO.getArtStyle() != null) {
            project.setArtStyle(projectEditDTO.getArtStyle());
        }

        projectRepository.save(project);
    }

    @Override
    public void updateInstructionList(ProjectInstructionUpdateDTO projectInstructionUpdateDTO) {
        Project project = getAndCheckProject(projectInstructionUpdateDTO.getProjectId());

        project.setInstructions(projectInstructionUpdateDTO.getInstructions());
        projectRepository.save(project);
    }

    @Override
    public void addInstructionString(ProjectInstructionUpdateDTO projectInstructionUpdateDTO) {
        Project project = getAndCheckProject(projectInstructionUpdateDTO.getProjectId());

        List<String> optimizedInstructions = promptOptimizationService.optimizeInstruction(projectInstructionUpdateDTO.getNewInstruction());
        project.getInstructions().addAll(optimizedInstructions);
        projectRepository.save(project);
    }

    @Override
    public ProjectDTO getProject(String projectId) {
        Project project = getAndCheckProject(projectId);

        return modelMapper.map(project, ProjectDTO.class);
    }

    @Override
    public List<ProjectDTO> getALlProjects() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<Project> projects = projectRepository.findAllByUserId(currentUser.getId());

        return projects.stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .toList();
    }

    private Project getAndCheckProject(String projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);

        if(project == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Project doesn't exist");
        }

        if(!project.getUserId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        return project;
    }
}
