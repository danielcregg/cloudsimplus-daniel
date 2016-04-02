/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.Vm;

import org.cloudbus.cloudsim.lists.PeList;

/**
 * VmSchedulerAbstract is an abstract class that represents the policy used by a
 * Virtual Machine Monitor (VMM) to share processing power of a PM among VMs
 * running in a host. Each host has to use is own instance of a
 * class that extends VmSchedulerAbstract that will so schedule the allocation of host's PEs for
 * VMs running on it.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmSchedulerAbstract implements VmScheduler {

    /**
     * The PEs of the host where the scheduler is associated.
     */
    private List<Pe> peList;

    /**
     * The map of VMs to PEs, where each key is a VM id and each value is a list
     * of PEs allocated to that VM.
     */
    private Map<String, List<Pe>> peMap;

    /**
     * The map of VMs to MIPS, were each key is a VM id and each value is the
     * currently allocated MIPS from the respective PE to that VM. The PEs where
     * the MIPS capacity is get are defined in the {@link #peMap}.
     *
     * @todo subclasses such as {@link VmSchedulerTimeShared} have an
     * {@link VmSchedulerTimeShared#mipsMapRequested} attribute that may be
     * confused with this one. So, the name of this one may be changed to
     * something such as allocatedMipsMap
     */
    private Map<String, List<Double>> mipsMap;

    /**
     * The total available MIPS that can be allocated on demand for VMs.
     */
    private double availableMips;

    /**
     * The VMs migrating in the host (arriving). It is the list of VM ids
     */
    private List<String> vmsMigratingIn;

    /**
     * The VMs migrating out the host (departing). It is the list of VM ids
     */
    private List<String> vmsMigratingOut;

    /**
     * Creates a new VmScheduler.
     *
     * @param pelist the list of PEs of the host where the VmScheduler is
     * associated to.
     * @pre peList != $null
     * @post $none
     */
    public VmSchedulerAbstract(List<Pe> pelist) {
        setPeList(pelist);
        setPeMap(new HashMap<>());
        setMipsMap(new HashMap<>());
        setAvailableMips(PeList.getTotalMips(getPeList()));
        setVmsMigratingIn(new ArrayList<>());
        setVmsMigratingOut(new ArrayList<>());
    }

    /**
     * Releases PEs allocated to all the VMs of the host the VmSchedulerAbstract
     * is associated to. After that, all PEs will be available to be used on
     * demand for requesting VMs.
     *
     * @pre $none
     * @post $none
     */
    @Override
    public void deallocatePesForAllVms() {
        getMipsMap().clear();
        setAvailableMips(PeList.getTotalMips(getPeList()));
        for (Pe pe : getPeList()) {
            pe.getPeProvisioner().deallocateMipsForAllVms();
        }
    }

    /**
     * Gets the pes allocated for a vm.
     *
     * @param vm the vm
     * @return the pes allocated for the given vm
     */
    @Override
    public List<Pe> getPesAllocatedForVM(Vm vm) {
        return getPeMap().get(vm.getUid());
    }

    /**
     * Returns the MIPS share of each host's Pe that is allocated to a given VM.
     *
     * @param vm the vm
     * @return an array containing the amount of MIPS of each pe that is
     * available to the VM
     * @pre $none
     * @post $none
     */
    @Override
    public List<Double> getAllocatedMipsForVm(Vm vm) {
        return getMipsMap().get(vm.getUid());
    }

    /**
     * Gets the total allocated MIPS for a VM along all its allocated PEs.
     *
     * @param vm the vm
     * @return the total allocated mips for the vm
     */
    @Override
    public double getTotalAllocatedMipsForVm(Vm vm) {
        double allocated = 0;
        List<Double> mipsMap = getAllocatedMipsForVm(vm);
        if (mipsMap != null) {
            for (double mips : mipsMap) {
                allocated += mips;
            }
        }
        return allocated;
    }

    /**
     * Returns maximum available MIPS among all the host's PEs.
     *
     * @return max mips
     */
    @Override
    public double getMaxAvailableMips() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }

        double max = 0.0;
        for (Pe pe : getPeList()) {
            double tmp = pe.getPeProvisioner().getAvailableMips();
            if (tmp > max) {
                max = tmp;
            }
        }

        return max;
    }

    /**
     * Returns PE capacity in MIPS.
     *
     * @return mips
     * @todo It considers that all PEs have the same capacity, what has been
     * shown doesn't be assured. The peList received by the VmScheduler can be
     * heterogeneous PEs.
     */
    @Override
    public double getPeCapacity() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }
        return getPeList().get(0).getMips();
    }

    /**
     * Gets the pe list.
     *
     * @return the pe list
     */
    @Override
    public final List<Pe> getPeList() {
        return peList;
    }

    /**
     * Sets the pe list.
     *
     * @param peList the pe list
     */
    protected final void setPeList(List<Pe> peList) {
        this.peList = peList;
    }

    /**
     * Gets the mips map.
     *
     * @return the mips map
     */
    protected Map<String, List<Double>> getMipsMap() {
        return mipsMap;
    }

    /**
     * Sets the mips map.
     *
     * @param mipsMap the mips map
     */
    protected final void setMipsMap(Map<String, List<Double>> mipsMap) {
        this.mipsMap = mipsMap;
    }

    /**
     * Gets the free mips.
     *
     * @return the free mips
     */
    @Override
    public double getAvailableMips() {
        return availableMips;
    }

    /**
     * Sets the free mips.
     *
     * @param availableMips the new free mips
     */
    protected final void setAvailableMips(double availableMips) {
        this.availableMips = availableMips;
    }

    /**
     * Gets the vms migrating out.
     *
     * @return the vms in migration
     */
    @Override
    public List<String> getVmsMigratingOut() {
        return vmsMigratingOut;
    }

    /**
     * Sets the vms migrating out.
     *
     * @param vmsInMigration the new vms migrating out
     */
    protected final void setVmsMigratingOut(List<String> vmsInMigration) {
        vmsMigratingOut = vmsInMigration;
    }

    /**
     * Gets the vms migrating in.
     *
     * @return the vms migrating in
     */
    @Override
    public List<String> getVmsMigratingIn() {
        return vmsMigratingIn;
    }

    /**
     * Sets the vms migrating in.
     *
     * @param vmsMigratingIn the new vms migrating in
     */
    protected final void setVmsMigratingIn(List<String> vmsMigratingIn) {
        this.vmsMigratingIn = vmsMigratingIn;
    }

    /**
     * Gets the pe map.
     *
     * @return the pe map
     */
    @Override
    public Map<String, List<Pe>> getPeMap() {
        return peMap;
    }

    /**
     * Sets the pe map.
     *
     * @param peMap the pe map
     */
    protected final void setPeMap(Map<String, List<Pe>> peMap) {
        this.peMap = peMap;
    }

}
