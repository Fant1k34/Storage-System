package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.PackageDto
import com.sklad.skladproject.dto.PackageRequestDto
import com.sklad.skladproject.services.PackageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Packages", description = "Packages items are bought in; unique by name + weight + weight unit")
@RestController
@RequestMapping("/")
class PackageController(
    val packageService: PackageService
) {
    private val logger = LoggerFactory.getLogger("PackageController")

    @Operation(summary = "Add a package", description = "Idempotent. `weightUnitId` must reference an existing measure unit, otherwise `false` is returned.")
    @ApiResponse(responseCode = "200", description = "`true` if the record exists after the call (created or already present), `false` on database failure")
    @ResponseBody
    @PostMapping("/v1/add-package")
    fun addPackage(@RequestBody packageRequest: PackageRequestDto): Boolean {
        logger.info("Handling add package request $packageRequest")

        val packageDomain = packageService.convertToPackage(packageRequest)
        val result = packageService.savePackage(packageDomain)

        return result.isSuccess
    }

    @Operation(summary = "List all packages", description = "Ordered by id, with the weight unit name resolved.")
    @ResponseBody
    @GetMapping("/v1/get-all-packages")
    fun getAllPackages(): List<PackageDto> {
        logger.info("Handling get all packages request")

        return packageService.getAllPackages()
    }

    // Package name is not unique (unique key is name + weight + unit), so a list is returned
    @Operation(summary = "Get packages by name", description = "One name may match several packages (different weight or unit), so a list is returned; empty when nothing matches.")
    @ResponseBody
    @GetMapping("/v1/get-package")
    fun getPackages(@Parameter(description = "Exact package name", example = "box") @RequestParam("name") name: String): List<PackageDto> {
        logger.info("Handling get packages request for name $name")

        return packageService.getPackagesByName(name)
    }
}
